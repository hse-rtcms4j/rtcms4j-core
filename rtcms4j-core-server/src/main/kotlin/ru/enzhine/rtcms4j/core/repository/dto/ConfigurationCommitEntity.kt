package ru.enzhine.rtcms4j.core.repository.dto

import java.time.OffsetDateTime

data class ConfigurationCommitEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val configurationId: Long,
    val sourceType: SourceType,
    val sourceIdentity: String,
    val commitHash: String,
    val jsonValues: String?,
    val jsonSchema: String?,
)
