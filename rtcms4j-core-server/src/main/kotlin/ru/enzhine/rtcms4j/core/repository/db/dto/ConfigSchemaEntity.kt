package ru.enzhine.rtcms4j.core.repository.db.dto

import java.time.OffsetDateTime

data class ConfigSchemaEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val configurationId: Long,
    val sourceType: SourceType,
    val sourceIdentity: String,
)
