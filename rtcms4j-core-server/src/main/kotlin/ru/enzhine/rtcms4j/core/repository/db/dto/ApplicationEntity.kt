package ru.enzhine.rtcms4j.core.repository.db.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ApplicationEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val namespaceId: Long,
    val creatorSub: UUID,
    var name: String,
    var description: String,
)
