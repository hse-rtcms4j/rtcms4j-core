package ru.enzhine.rtcms4j.core.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationEntity

interface ConfigurationEntityRepository {
    fun save(configurationEntity: ConfigurationEntity): ConfigurationEntity

    fun findAllByApplicationIdAndName(
        applicationId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ConfigurationEntity>

    fun findById(id: Long): ConfigurationEntity?

    fun removeById(id: Long): Boolean
}
