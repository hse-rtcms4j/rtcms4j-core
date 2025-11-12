package ru.enzhine.rtcms4j.core.repository.dto

import java.time.OffsetDateTime

data class ConfigurationSyncStateEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val configurationId: Long,
    val sourceIdentity: String,
    val commitHash: String?,
    val isOnline: Boolean?,
)
