package ru.enzhine.rtcms4j.core.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.enzhine.rtcms4j.core.builder.applicationNotFoundException
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
import ru.enzhine.rtcms4j.core.service.dto.Configuration
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.dto.SourceType
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
            throw applicationNotFoundException(application.id)
        }
    }

    @Transactional
    override fun getConfigurationById(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        forShare: Boolean,
    ): ConfigurationDetailed {
        val application = applicationService.getApplicationById(namespaceId, applicationId, true)

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
                configurationCommitEntityRepository.findByConfigurationIdAndCommitHash(
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
        configurationId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Configuration> {
        TODO("Not yet implemented")
    }

    override fun deleteConfiguration(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun applyConfigurationByCommit(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): Configuration {
        TODO("Not yet implemented")
    }

    override fun commitValuesAndSchema(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        valuesData: String?,
        schemaData: String?,
    ): ConfigurationCommitDetailed {
        TODO("Not yet implemented")
    }

    override fun getConfigurationCommitByHash(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitDetailed {
        TODO("Not yet implemented")
    }

    override fun getConfigurationCommits(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): List<ConfigurationCommit> {
        TODO("Not yet implemented")
    }
}
