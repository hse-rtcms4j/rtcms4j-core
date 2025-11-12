package ru.enzhine.rtcms4j.core.repository

import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationSyncStateEntity
import java.time.OffsetDateTime

interface ConfigurationSyncStateEntityRepository {
    fun save(configurationSyncStateEntity: ConfigurationSyncStateEntity): ConfigurationSyncStateEntity

    fun findAllByConfigurationIdAndAfterTimestamp(
        configurationId: Long,
        timestamp: OffsetDateTime,
    ): List<ConfigurationSyncStateEntity>

    fun findById(id: Long): ConfigurationSyncStateEntity?

    fun removeById(id: Long): Boolean

    fun removeAllByConfigurationIdAndBeforeTimestamp(
        configurationId: Long,
        timestamp: OffsetDateTime,
    ): Int
}
