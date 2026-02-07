package ru.enzhine.rtcms4j.core.service.external.dto

import java.util.UUID

data class KeycloakUser(
    val subject: UUID,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
)
