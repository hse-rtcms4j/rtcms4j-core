package ru.enzhine.rtcms4j.core.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.service.dto.Configuration
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.dto.SourceType
import java.util.UUID

interface ConfigurationService {
    @Throws(ConditionFailureException.NotFound::class, ConditionFailureException.KeyDuplicated::class)
    fun createConfiguration(
        creator: UUID,
        namespaceId: Long,
        applicationId: Long,
        name: String,
        schemaSourceType: SourceType,
    ): ConfigurationDetailed

    @Throws(ConditionFailureException.NotFound::class)
    fun getConfigurationById(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        forShare: Boolean,
    ): ConfigurationDetailed

    @Throws(ConditionFailureException.NotFound::class, ConditionFailureException.KeyDuplicated::class)
    fun updateConfiguration(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        name: String?,
        schemaSourceType: SourceType?,
    ): Configuration

    @Throws(ConditionFailureException.NotFound::class)
    fun findConfigurations(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Configuration>

    @Throws(ConditionFailureException.NotFound::class)
    fun deleteConfiguration(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): Boolean

    @Throws(ConditionFailureException.NotFound::class)
    fun applyConfigurationByCommit(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): Configuration

    @Throws(ConditionFailureException.NotFound::class, ConditionFailureException.KeyDuplicated::class)
    fun commitValuesAndSchema(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        valuesData: String?,
        schemaData: String?,
    ): ConfigurationCommitDetailed

    @Throws(ConditionFailureException.NotFound::class)
    fun getConfigurationCommitByHash(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitDetailed

    @Throws(ConditionFailureException.NotFound::class)
    fun getConfigurationCommits(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): List<ConfigurationCommit>
}
