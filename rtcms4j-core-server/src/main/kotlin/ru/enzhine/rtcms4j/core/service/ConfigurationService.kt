package ru.enzhine.rtcms4j.core.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.service.dto.Configuration
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationDetailed
import java.util.UUID

interface ConfigurationService {
    fun createConfiguration(
        creator: UUID,
        namespaceId: Long,
        applicationId: Long,
        name: String,
    ): ConfigurationDetailed

    fun getConfigurationById(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): ConfigurationDetailed

    fun updateConfiguration(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): Configuration

    fun findConfigurations(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Configuration>

    fun deleteConfiguration(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): Boolean

    fun applyConfigurationByCommit(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): Configuration

    fun getConfigurationCommitByHash(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitDetailed

    fun getConfigurationCommits(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
    ): List<ConfigurationCommit>

    fun commitValuesAndSchema(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        valuesData: String?,
        schemaData: String?,
    ): ConfigurationCommitDetailed
}
