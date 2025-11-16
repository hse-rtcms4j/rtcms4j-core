package ru.enzhine.rtcms4j.core.repository.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ConfigurationEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val applicationId: Long,
    val creatorSub: UUID,
    var name: String,
    var commitHash: String?,
    var streamKey: String?,
    var schemaSourceType: SourceType,
)
