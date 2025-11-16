package ru.enzhine.rtcms4j.core.repository

import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitEntity

interface ConfigurationCommitEntityRepository {
    fun save(configurationCommitDetailedEntity: ConfigurationCommitDetailedEntity): ConfigurationCommitDetailedEntity

    fun findAllByConfigurationId(configurationId: Long): List<ConfigurationCommitEntity>

    fun findByConfigurationIdAndCommitHash(
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitDetailedEntity?

    fun findById(id: Long): ConfigurationCommitDetailedEntity?

    fun removeById(id: Long): Boolean
}
