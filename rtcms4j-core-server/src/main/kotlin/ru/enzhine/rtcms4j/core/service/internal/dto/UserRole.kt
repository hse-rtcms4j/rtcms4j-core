package ru.enzhine.rtcms4j.core.service.internal.dto

import java.util.UUID

data class UserRole(
    val subject: UUID,
    val assignerSubject: UUID,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
)
