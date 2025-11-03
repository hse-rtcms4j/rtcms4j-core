package ru.enzhine.rtcms4j.core.repository.dto

import java.time.OffsetDateTime
import java.util.UUID

data class NamespaceEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val creatorSub: UUID,
    val name: String,
    val description: String,
)
