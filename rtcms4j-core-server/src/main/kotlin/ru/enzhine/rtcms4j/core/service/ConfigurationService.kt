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
        id: Long,
    ): ConfigurationDetailed?

    fun updateConfiguration(configuration: Configuration): Configuration

    fun findConfigurations(
        namespaceId: Long,
        applicationId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Configuration>

    fun deleteConfiguration(id: Long): Boolean

    fun applyConfigurationByCommit(
        configurationId: Long,
        commitHash: String,
    ): Configuration?

    fun getConfigurationCommitByHash(
        namespaceId: Long,
        applicationId: Long,
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitDetailed?

    fun getConfigurationCommits(configuration: Configuration): List<ConfigurationCommit>

    fun commitValuesAndSchema(
        configuration: Configuration,
        valuesData: String?,
        schemaData: String?,
    ): ConfigurationCommitDetailed
}
