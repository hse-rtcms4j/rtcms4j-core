package ru.enzhine.rtcms4j.core.security.dto

import java.util.UUID

data class KeycloakPrincipal(
    val sub: UUID,
    val username: String?,
    val clientId: String?,
    val roles: Set<String>,
) {
    val isClient: Boolean
        get() = username == null
}
