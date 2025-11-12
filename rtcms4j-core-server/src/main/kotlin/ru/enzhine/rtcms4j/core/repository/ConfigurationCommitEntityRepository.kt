package ru.enzhine.rtcms4j.core.repository

import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitAppliedEntity
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitEntity

interface ConfigurationCommitEntityRepository {
    fun save(configurationCommitEntity: ConfigurationCommitEntity): ConfigurationCommitEntity

    fun findAllByConfigurationId(configurationId: Long): List<ConfigurationCommitAppliedEntity>

    fun findAllByConfigurationIdAndCommitHash(
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitEntity?

    fun findById(id: Long): ConfigurationCommitEntity?

    fun removeById(id: Long): Boolean
}
