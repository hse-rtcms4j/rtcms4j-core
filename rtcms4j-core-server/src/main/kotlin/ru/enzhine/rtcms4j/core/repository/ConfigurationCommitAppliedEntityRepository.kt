package ru.enzhine.rtcms4j.core.repository

import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitAppliedEntity

interface ConfigurationCommitAppliedEntityRepository {
    fun save(configurationCommitAppliedEntity: ConfigurationCommitAppliedEntity): ConfigurationCommitAppliedEntity

    fun findAllByConfigurationId(configurationId: Long): List<ConfigurationCommitAppliedEntity>

    fun findById(id: Long): ConfigurationCommitAppliedEntity?

    fun removeById(id: Long): Boolean
}
