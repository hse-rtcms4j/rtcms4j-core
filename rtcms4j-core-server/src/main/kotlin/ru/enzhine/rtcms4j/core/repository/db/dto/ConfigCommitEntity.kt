package ru.enzhine.rtcms4j.core.repository.db.dto

import java.time.OffsetDateTime

data class ConfigCommitEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val configSchemaId: Long,
    val configurationId: Long,
    val sourceType: SourceType,
    val sourceIdentity: String,
    val version: String,
)
