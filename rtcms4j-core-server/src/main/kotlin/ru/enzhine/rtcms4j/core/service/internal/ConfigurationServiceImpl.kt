package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.enzhine.rtcms4j.core.builder.applicationNotFoundException
import ru.enzhine.rtcms4j.core.builder.configurationCommitNotFoundException
import ru.enzhine.rtcms4j.core.builder.configurationNotFoundException
import ru.enzhine.rtcms4j.core.builder.nameKeyDuplicatedException
import ru.enzhine.rtcms4j.core.builder.newConfigurationEntity
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.mapper.toDetailed
import ru.enzhine.rtcms4j.core.mapper.toRepository
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.db.ConfigurationCommitEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.ConfigurationEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier
import ru.enzhine.rtcms4j.core.repository.kv.KeyValueRepository
import ru.enzhine.rtcms4j.core.repository.kv.PubSubProducer
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType
import java.util.UUID

@Service
class ConfigurationServiceImpl(
    private val configurationEntityRepository: ConfigurationEntityRepository,
    private val configurationCommitEntityRepository: ConfigurationCommitEntityRepository,
    private val keyValueRepository: KeyValueRepository,
    private val pubSubProducer: PubSubProducer,
    private val defaultPaginationProperties: DefaultPaginationProperties,
    private val applicationService: ApplicationService,
) : ConfigurationService {
    @Transactional
    override fun createConfiguration(
        creator: UUID,
        namespaceId: Long,
        applicationId: Long,
        name: String,
        schemaSourceType: SourceType,
    ): ConfigurationDetailed {
        val application = applicationService.getApplicationById(namespaceId, applicationId, true)

        try {
            val configurationEntity =
                configurationEntityRepository.save(
                    newConfigurationEntity(
                        applicationId = application.id,
                        creatorSub = creator,
                        name = name,
                        schemaSourceType = schemaSourceType.toRepository(),
                        commitHash = null,
                    ),
                )

            return configurationEntity
                .toService(application.namespaceId)
                .toDetailed(
                    valuesData = null,
                    schemaData = null,
                )
        } catch (ex: DuplicateKeyException) {
            throw nameKeyDuplicatedException(ex)
        } catch (_: DataIntegrityViolationException) {
            throw applicationNotFoundException(applicationId)
        }
    }

    override fun getConfigurationById(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        forShare: Boolean,
    ): ConfigurationDetailed {
        val application = applicationService.getApplicationById(namespaceId, applicationId, forShare)

        val configuration =
            configurationEntityRepository
                .findById(
                    id = configurationId,
                    modifier = if (forShare) QueryModifier.FOR_SHARE else QueryModifier.NONE,
                )?.toService(application.namespaceId)
                ?: throw configurationNotFoundException(configurationId)

        if (configuration.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        var valuesData = keyValueRepository.getConfigurationValues(configuration)
        var schemaData = keyValueRepository.getConfigurationSchema(configuration)

        if (valuesData == null && schemaData == null && configuration.commitHash != null) {
            val commit =
                configurationCommitEntityRepository.findByConfigurationIdAndCommitHashDetailed(
                    configurationId = configuration.id,
                    commitHash = configuration.commitHash,
                )

            valuesData = commit?.jsonValues
            schemaData = commit?.jsonSchema
        }

        return configuration.toDetailed(
            valuesData = valuesData,
            schemaData = schemaData,
        )
    }

    @Transactional
    override fun updateConfiguration(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        name: String?,
        schemaSourceType: SourceType?,
    ): Configuration {
        val application = applicationService.getApplicationById(namespaceId, applicationId, true)

        val configurationEntity =
            configurationEntityRepository.findById(configurationId, QueryModifier.FOR_UPDATE)
                ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        if (name == null && schemaSourceType == null) {
            return configurationEntity.toService(application.namespaceId)
        }

        name?.let { configurationEntity.name = it }
        schemaSourceType?.let { configurationEntity.schemaSourceType = schemaSourceType.toRepository() }

        try {
            val updatedConfigurationEntity =
                configurationEntityRepository.update(configurationEntity)

            return updatedConfigurationEntity.toService(application.namespaceId)
        } catch (ex: DuplicateKeyException) {
            throw nameKeyDuplicatedException(ex)
        }
    }

    override fun findConfigurations(
        namespaceId: Long,
        applicationId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Configuration> {
        val application = applicationService.getApplicationById(namespaceId, applicationId, false)

        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return configurationEntityRepository
            .findAllByApplicationIdAndName(application.id, name, pageable)
            .map { it.toService(namespaceId) }
    }

    override fun deleteConfiguration(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): Boolean {
        val application = applicationService.getApplicationById(namespaceId, applicationId, false)

        val configurationEntity =
            configurationEntityRepository.findById(configurationId, QueryModifier.FOR_UPDATE)
                ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        return configurationEntityRepository.removeById(configurationEntity.id)
    }

    @Transactional
    override fun applyConfigurationByCommit(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): Configuration {
        val application = applicationService.getApplicationById(namespaceId, applicationId, true)

        val configurationEntity =
            configurationEntityRepository.findById(configurationId, QueryModifier.FOR_UPDATE)
                ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        val commitEntity =
            configurationCommitEntityRepository.findByConfigurationIdAndCommitHash(
                configurationId = configurationEntity.id,
                commitHash = commitHash,
            ) ?: throw configurationCommitNotFoundException(configurationId, commitHash)

        if (commitEntity.configurationId != configurationEntity.id) {
            throw configurationCommitNotFoundException(configurationId, commitHash)
        }

        // TODO: commit broadcast
        TODO("WIP")
//        configurationEntity.commitHash = commitEntity.commitHash
//        configurationEntityRepository.update(configurationEntity)
    }

    @Transactional
    override fun commitValuesAndSchema(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        valuesData: String?,
        schemaData: String?,
    ): ConfigurationCommitDetailed {
        val application = applicationService.getApplicationById(namespaceId, applicationId, true)

        val configurationEntity =
            configurationEntityRepository.findById(configurationId, QueryModifier.FOR_UPDATE)
                ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        // TODO: commit creation and broadcast
        TODO("WIP")
    }

    override fun getConfigurationCommitByHash(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
        forShare: Boolean,
    ): ConfigurationCommitDetailed {
        val application = applicationService.getApplicationById(namespaceId, applicationId, forShare)

        val configurationEntity =
            configurationEntityRepository.findById(
                id = configurationId,
                modifier = if (forShare) QueryModifier.FOR_SHARE else QueryModifier.NONE,
            ) ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        val commit =
            configurationCommitEntityRepository
                .findByConfigurationIdAndCommitHashDetailed(
                    configurationId = configurationEntity.id,
                    commitHash = commitHash,
                )?.toService(
                    namespaceId = application.namespaceId,
                    applicationId = application.id,
                ) ?: throw configurationCommitNotFoundException(configurationId, commitHash)

        if (commit.configurationId != configurationEntity.id) {
            throw configurationCommitNotFoundException(configurationId, commitHash)
        }

        return commit
    }

    override fun getConfigurationCommits(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): List<ConfigurationCommit> {
        val application = applicationService.getApplicationById(namespaceId, applicationId, false)

        val configurationEntity =
            configurationEntityRepository.findById(configurationId)
                ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        return configurationCommitEntityRepository
            .findAllByConfigurationId(configurationEntity.id)
            .map { it.toService(application.namespaceId, application.id) }
    }
}
