package ru.enzhine.rtcms4j.core.security.dto

import java.util.UUID

data class KeycloakPrincipal(
    val sub: UUID,
    val username: String?,
    val clientId: String?,
    val roles: Set<String>,
    val namespaceId: Long?,
    val applicationId: Long?,
) {
    val isClient: Boolean
        get() = namespaceId != null && applicationId != null
}
