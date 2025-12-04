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
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.json.JsonNormalizer
import ru.enzhine.rtcms4j.core.json.JsonSchemaValidator
import ru.enzhine.rtcms4j.core.mapper.toDetailed
import ru.enzhine.rtcms4j.core.mapper.toRepository
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.db.ConfigCommitEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.ConfigSchemaEntityRepository
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
    private val configSchemaEntityRepository: ConfigSchemaEntityRepository,
    private val configCommitEntityRepository: ConfigCommitEntityRepository,
    private val defaultPaginationProperties: DefaultPaginationProperties,
    private val applicationService: ApplicationService,
    private val keyValueRepository: KeyValueRepository,
    private val pubSubProducer: PubSubProducer,
    private val jsonNormalizer: JsonNormalizer,
    private val jsonSchemaValidator: JsonSchemaValidator,
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
                        actualCommitId = null,
                    ),
                )

            return configurationEntity
                .toService(application.namespaceId)
                .toDetailed(
                    jsonSchema = null,
                    jsonValues = null,
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

        val actualCommitId = configuration.actualCommitId
        if (actualCommitId == null) {
            return configuration
                .toDetailed(
                    jsonSchema = null,
                    jsonValues = null,
                )
        }

        // cached
        var jsonSchema = keyValueRepository.getConfigurationSchema(configuration)
        var jsonValues = keyValueRepository.getConfigurationValues(configuration)

        if (jsonSchema == null || jsonValues == null) {
            val valuesCommit = configCommitEntityRepository.findById(actualCommitId)
            jsonValues = valuesCommit?.jsonValues
            jsonSchema =
                valuesCommit?.let {
                    configSchemaEntityRepository.findById(it.configSchemaId)?.jsonSchema
                }
        }

        return configuration.toDetailed(
            jsonSchema = jsonSchema,
            jsonValues = jsonValues,
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
    override fun applyConfigurationByCommitId(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitId: Long,
    ): Configuration {
        val application = applicationService.getApplicationById(namespaceId, applicationId, true)

        val configurationEntity =
            configurationEntityRepository.findById(configurationId, QueryModifier.FOR_UPDATE)
                ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
        }

        val configCommitEntity =
            configCommitEntityRepository.findById(commitId)
                ?: throw configurationCommitNotFoundException(configurationId, commitId)

        val configSchemaEntity =
            configSchemaEntityRepository.findById(configCommitEntity.configSchemaId)
                ?: throw RuntimeException(
                    "Schema with id '${configCommitEntity.configSchemaId}' expected to exist" +
                        " for commit with id '${configCommitEntity.id}'",
                )

        if (configSchemaEntity.configurationId != configurationEntity.id) {
            throw configurationCommitNotFoundException(configurationId, commitId)
        }

        // TODO: update redis cache, commit broadcast
        TODO("WIP")
        //        configurationEntity.commitHash = commitEntity.commitHash
        //        configurationEntityRepository.update(configurationEntity)
    }

    @Transactional
    override fun commitValuesAndSchema(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        sourceType: SourceType,
        sourceIdentity: String,
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

        if (valuesData == null && schemaData == null) {
            throw ConditionFailureException("Schema or values must be provided.", null, null)
        }

        val configurationSchemaSourceType = configurationEntity.schemaSourceType.toService()
        if (configurationSchemaSourceType != sourceType) {
            throw ConditionFailureException(
                message =
                    "Configuration schema push by $sourceType with identity $sourceIdentity" +
                        "is prohibited, due to config source $configurationSchemaSourceType limitation.",
                cause = null,
                detailCode = null,
            )
        }

        // TODO: rework

        var foundSchema: String? = null
        // provided schema
        if (schemaData != null) {
            val normalizedJsonSchema = jsonNormalizer.normalize(schemaData)
            jsonSchemaValidator.validateSchema(normalizedJsonSchema)
            foundSchema = normalizedJsonSchema
        }
        // redis cache
        if (foundSchema == null) {
            val cached =
                keyValueRepository.getConfigurationSchema(
                    configuration = configurationEntity.toService(application.namespaceId),
                )
            foundSchema = cached
        }
        // actual db schema
        val actualCommitId = configurationEntity.actualCommitId
        if (foundSchema == null && actualCommitId != null) {
            val configCommitEntity =
                configCommitEntityRepository.findById(actualCommitId)
                    ?: throw RuntimeException(
                        "Commit with id '$actualCommitId' expected to exist" +
                            " for configuration with id '${configurationEntity.id}'",
                    )

            val configSchemaEntity =
                configSchemaEntityRepository.findById(configCommitEntity.configSchemaId)
                    ?: throw RuntimeException(
                        "Schema with id '${configCommitEntity.configSchemaId}' expected to exist" +
                            " for commit with id '${configCommitEntity.id}'",
                    )

            foundSchema = configSchemaEntity.jsonSchema
        }

        val jsonSchema =
            foundSchema
                ?: throw ConditionFailureException(
                    message = "No schema provided or ever existed for given values.",
                    cause = null,
                    detailCode = null,
                )

        val jsonValues =
            valuesData?.let {
                val normalizedJsonValues = jsonNormalizer.normalize(it)
                jsonSchemaValidator.validateValuesBySchema(normalizedJsonValues, jsonSchema)
                normalizedJsonValues
            }

        try {
//            val commitEntity =
//                configurationCommitEntityRepository.save(
//                    newConfigurationCommitEntity(
//                        configurationId = configurationEntity.id,
//                        sourceType = configurationEntity.schemaSourceType,
//                        sourceIdentity = sourceIdentity,
//                        commitHash = currentCommitHash,
//                        jsonValues = jsonValues,
//                        jsonSchema = jsonSchema,
//                    ),
//                )

            TODO("update configuration commitHash, update redis cache, commit broadcast")

//            return commitEntity
//                .toService(
//                    namespaceId = application.namespaceId,
//                    applicationId = application.id,
//                )
        } catch (ex: DuplicateKeyException) {
            throw ConditionFailureException(
                message = "Commit idempotency failure.",
                cause = ex,
                detailCode = null,
            )
        } catch (_: DataIntegrityViolationException) {
            throw configurationNotFoundException(applicationId)
        }
    }

    override fun getConfigurationCommitByCommitId(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitId: Long,
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

        val configCommitEntity =
            configCommitEntityRepository.findById(commitId)
                ?: throw configurationCommitNotFoundException(configurationId, commitId)

        val configSchemaEntity =
            configSchemaEntityRepository.findById(configCommitEntity.configSchemaId)
                ?: throw RuntimeException(
                    "Schema with id '${configCommitEntity.configSchemaId}' expected to exist" +
                        " for commit with id '${configCommitEntity.id}'",
                )

        if (configSchemaEntity.configurationId != configurationEntity.id) {
            throw configurationCommitNotFoundException(configurationId, commitId)
        }

        return configCommitEntity.toService(
            namespaceId = application.namespaceId,
            applicationId = application.id,
            configurationId = configurationEntity.id,
            jsonSchema = configSchemaEntity.jsonSchema,
        )
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

        return configCommitEntityRepository
            .findAllByConfigurationId(configurationEntity.id)
            .map {
                it.toService(
                    namespaceId = application.namespaceId,
                    applicationId = application.id,
                    configurationId = configurationEntity.id,
                )
            }
    }
}
