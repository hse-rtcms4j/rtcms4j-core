package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType
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
    fun applyConfigurationByCommitId(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitId: Long,
    ): ConfigurationDetailed

    @Throws(ConditionFailureException.NotFound::class, ConditionFailureException.KeyDuplicated::class)
    fun commitValuesAndSchema(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        sourceType: SourceType,
        sourceIdentity: String,
        jsonSchema: String?,
        jsonValues: String,
    ): ConfigurationCommitDetailed

    @Throws(ConditionFailureException.NotFound::class)
    fun getConfigurationCommitByCommitId(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitId: Long,
        forShare: Boolean,
    ): ConfigurationCommitDetailed

    @Throws(ConditionFailureException.NotFound::class)
    fun getConfigurationCommits(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        pageable: Pageable?,
    ): Page<ConfigurationCommit>

    @Throws(ConditionFailureException.NotFound::class)
    fun deleteConfigurationCommit(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitId: Long,
    ): Boolean
}
