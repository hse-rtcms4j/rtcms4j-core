package ru.enzhine.rtcms4j.core.repository

import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationEntity

interface ConfigurationEntityRepository {
    fun save(configurationEntity: ConfigurationEntity): ConfigurationEntity

    fun findAllByApplicationId(applicationId: Long): List<ConfigurationEntity>

    fun findById(id: Long): ConfigurationEntity?

    fun removeById(id: Long): Boolean
}
