package ru.enzhine.rtcms4j.core.repository.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ApplicationManagerEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val applicationId: Long,
    val assignerSub: UUID,
    val userSub: UUID,
)
