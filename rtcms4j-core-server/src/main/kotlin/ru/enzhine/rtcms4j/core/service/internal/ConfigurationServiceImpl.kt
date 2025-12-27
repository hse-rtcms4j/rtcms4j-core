package ru.enzhine.rtcms4j.core.service.internal

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.enzhine.rtcms4j.core.builder.applicationNotFoundException
import ru.enzhine.rtcms4j.core.builder.configSchemaDuplicatedException
import ru.enzhine.rtcms4j.core.builder.configValuesDuplicatedException
import ru.enzhine.rtcms4j.core.builder.configurationCommitNotFoundException
import ru.enzhine.rtcms4j.core.builder.configurationNotFoundException
import ru.enzhine.rtcms4j.core.builder.nameKeyDuplicatedException
import ru.enzhine.rtcms4j.core.builder.newConfigCommitDetailedEntity
import ru.enzhine.rtcms4j.core.builder.newConfigSchemaDetailedEntity
import ru.enzhine.rtcms4j.core.builder.newConfigurationEntity
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.json.JsonSchemaValidator
import ru.enzhine.rtcms4j.core.json.JsonValuesExtractor
import ru.enzhine.rtcms4j.core.mapper.toDetailed
import ru.enzhine.rtcms4j.core.mapper.toRepository
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.db.ConfigCommitEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.ConfigSchemaEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.ConfigurationEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier
import ru.enzhine.rtcms4j.core.repository.kv.KeyValueRepository
import ru.enzhine.rtcms4j.core.repository.kv.PubSubProducer
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonSchema
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonValues
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheKey
import ru.enzhine.rtcms4j.core.repository.kv.dto.NotifyEventDto
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType
import ru.enzhine.rtcms4j.core.service.internal.tx.registerCommitCallback
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
    private val jsonSchemaValidator: JsonSchemaValidator,
    private val jsonValuesExtractor: JsonValuesExtractor,
) : ConfigurationService {
    private val logger = LoggerFactory.getLogger(this::class.java)

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
                        actualCommitVersion = null,
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

        // cache retrieve attempt
        val cacheKey =
            CacheKey(
                namespaceId = application.namespaceId,
                applicationId = configuration.applicationId,
                configurationId = configuration.id,
            )
        var jsonSchema = keyValueRepository.getCacheJsonSchema(cacheKey)?.jsonSchema
        var jsonValues = keyValueRepository.getCacheJsonValues(cacheKey)?.jsonValues

        // database retrieve attempt
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

    @Transactional
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
    ): ConfigurationDetailed {
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

        if (configCommitEntity.configurationId != configurationEntity.id) {
            throw configurationCommitNotFoundException(configurationId, commitId)
        }

        val configSchemaEntity =
            configSchemaEntityRepository.findById(configCommitEntity.configSchemaId)
                ?: throw RuntimeException(
                    "Schema with id '${configCommitEntity.configSchemaId}' expected to exist" +
                        " for commit with id '${configCommitEntity.id}'",
                )

        if (configSchemaEntity.configurationId != configurationEntity.id) {
            throw configurationCommitNotFoundException(configurationId, commitId)
        }

        val cacheKey =
            CacheKey(
                namespaceId = application.namespaceId,
                applicationId = application.id,
                configurationId = configurationEntity.id,
            )

        val commitVersion = jsonValuesExtractor.validateAndGetVersion(configCommitEntity.jsonValues)

        try {
            configurationEntity.actualCommitId = configCommitEntity.id
            configurationEntity.actualCommitVersion = commitVersion
            configurationEntityRepository.update(configurationEntity)
        } catch (ex: Throwable) {
            throw RuntimeException(
                "Unable to commit id '${configCommitEntity.id}' due to unknown problem",
                ex,
            )
        }

        registerCommitCallback {
            cacheAndBroadcast(
                cacheKey = cacheKey,
                namespaceId = application.namespaceId,
                applicationId = application.id,
                configurationId = configurationEntity.id,
                jsonSchemaId = configSchemaEntity.id,
                jsonSchema = configSchemaEntity.jsonSchema,
                jsonValues = configCommitEntity.jsonValues,
            )
        }

        return configurationEntity
            .toService(application.namespaceId)
            .toDetailed(
                jsonSchema = configSchemaEntity.jsonSchema,
                jsonValues = configCommitEntity.jsonValues,
            )
    }

    @Transactional
    override fun commitValuesAndSchema(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        sourceType: SourceType,
        sourceIdentity: String,
        jsonSchema: String?,
        jsonValues: String,
    ): ConfigurationCommitDetailed {
        val application = applicationService.getApplicationById(namespaceId, applicationId, true)

        val configurationEntity =
            configurationEntityRepository.findById(configurationId, QueryModifier.FOR_UPDATE)
                ?: throw configurationNotFoundException(configurationId)

        if (configurationEntity.applicationId != application.id) {
            throw configurationNotFoundException(configurationId)
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

        val cacheKey =
            CacheKey(
                namespaceId = application.namespaceId,
                applicationId = application.id,
                configurationId = configurationEntity.id,
            )

        var currentJsonSchemaId: Long? = null
        var currentJsonSchema: String? = null

        if (jsonSchema == null) {
            // find existing schema
            val actualCommitId = configurationEntity.actualCommitId
            if (actualCommitId == null) {
                throw ConditionFailureException(
                    message = "Configuration schema not given as well as not ever existed to commit values.",
                    cause = null,
                    detailCode = null,
                )
            }

            // cache retrieve attempt
            val cache = keyValueRepository.getCacheJsonSchema(cacheKey)
            if (cache != null) {
                currentJsonSchemaId = cache.jsonSchemaId
                currentJsonSchema = cache.jsonSchema
            }

            // db retrieve attempt
            if (currentJsonSchemaId == null) {
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

                currentJsonSchemaId = configSchemaEntity.id
                currentJsonSchema = configSchemaEntity.jsonSchema
            }
        } else {
            // post new schema
            jsonSchemaValidator.validateSchema(jsonSchema)

            val configSchemaEntity =
                try {
                    configSchemaEntityRepository.save(
                        newConfigSchemaDetailedEntity(
                            configurationId = configurationEntity.id,
                            sourceType = sourceType.toRepository(),
                            sourceIdentity = sourceIdentity,
                            jsonSchema = jsonSchema,
                        ),
                    )
                } catch (ex: DuplicateKeyException) {
                    throw configSchemaDuplicatedException(ex, configurationEntity.id)
                } catch (_: DataIntegrityViolationException) {
                    throw configurationNotFoundException(applicationId)
                }

            currentJsonSchemaId = configSchemaEntity.id
            currentJsonSchema = configSchemaEntity.jsonSchema
        }

        jsonSchemaValidator.validateValuesBySchema(jsonValues, currentJsonSchema!!)
        val commitVersion = jsonValuesExtractor.validateAndGetVersion(jsonValues)

        val configCommitEntity =
            try {
                configCommitEntityRepository.save(
                    newConfigCommitDetailedEntity(
                        configSchemaId = currentJsonSchemaId,
                        configurationId = configurationEntity.id,
                        sourceType = sourceType.toRepository(),
                        sourceIdentity = sourceIdentity,
                        jsonValues = jsonValues,
                    ),
                )
            } catch (ex: DuplicateKeyException) {
                throw configValuesDuplicatedException(ex, configurationEntity.id)
            } catch (ex: DataIntegrityViolationException) {
                throw RuntimeException("Schema with id '$currentJsonSchemaId' expected to exist.", ex)
            }

        try {
            configurationEntity.actualCommitId = configCommitEntity.id
            configurationEntity.actualCommitVersion = commitVersion
            configurationEntityRepository.update(configurationEntity)
        } catch (ex: Throwable) {
            throw RuntimeException(
                "Unable to commit id '${configCommitEntity.id}' due to unknown problem",
                ex,
            )
        }

        registerCommitCallback {
            cacheAndBroadcast(
                cacheKey = cacheKey,
                namespaceId = application.namespaceId,
                applicationId = application.id,
                configurationId = configurationEntity.id,
                jsonSchemaId = currentJsonSchemaId,
                jsonSchema = currentJsonSchema,
                jsonValues = configCommitEntity.jsonValues,
            )
        }

        return configCommitEntity
            .toService(
                namespaceId = application.namespaceId,
                applicationId = application.id,
                configurationId = configurationEntity.id,
                jsonSchema = currentJsonSchema,
            )
    }

    private fun cacheAndBroadcast(
        cacheKey: CacheKey,
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        jsonSchemaId: Long,
        jsonSchema: String,
        jsonValues: String,
    ) {
        try {
            keyValueRepository.putCacheJsonSchema(
                cacheKey = cacheKey,
                cacheJsonSchema =
                    CacheJsonSchema(
                        jsonSchemaId = jsonSchemaId,
                        jsonSchema = jsonSchema,
                    ),
            )
        } catch (ex: Throwable) {
            logger.error("Unable to put cache json schema for configuration with id $configurationId", ex)
        }

        try {
            keyValueRepository.putCacheJsonValues(
                cacheKey = cacheKey,
                cacheJsonValues =
                    CacheJsonValues(
                        jsonValues = jsonValues,
                    ),
            )
        } catch (ex: Throwable) {
            logger.error("Unable to update cache json values for configuration with id $configurationId", ex)
        }

        try {
            pubSubProducer.publishEvent(
                NotifyEventDto(
                    namespaceId = namespaceId,
                    applicationId = applicationId,
                    eventType = NotifyEventDto.EventType.CONFIGURATION,
                    content = jsonValues,
                ),
            )
        } catch (ex: Throwable) {
            logger.error("Unable to publish pub/sub event for configuration with id $configurationId", ex)
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

    @Transactional
    override fun deleteConfigurationCommit(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitId: Long,
    ): Boolean {
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

        if (configCommitEntity.configurationId != configurationEntity.id) {
            throw configurationCommitNotFoundException(configurationId, commitId)
        }

        if (configCommitEntity.id == configurationEntity.actualCommitId) {
            throw ConditionFailureException(
                message = "It is prohibited to delete commit that is in use.",
                cause = null,
                detailCode = null,
            )
        }

        if (!configCommitEntityRepository.removeById(configCommitEntity.id)) {
            throw RuntimeException(
                "Commit with id '${configCommitEntity.id}' expected to exist, " +
                    "however deletion failed.",
            )
        }

        val relatedCommits =
            configCommitEntityRepository.findAllByConfigSchemaId(configCommitEntity.configSchemaId)
        if (!relatedCommits.isEmpty()) {
            return true
        }

        if (!configSchemaEntityRepository.removeById(configCommitEntity.configSchemaId)) {
            throw RuntimeException(
                "Commit schema with id '${configCommitEntity.configSchemaId}' expected to exist, " +
                    "however deletion failed.",
            )
        }

        return true
    }
}
