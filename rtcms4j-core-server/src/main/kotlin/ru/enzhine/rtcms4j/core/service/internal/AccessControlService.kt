package ru.enzhine.rtcms4j.core.service.internal

import ru.enzhine.rtcms4j.core.security.dto.KeycloakPrincipal

interface AccessControlService {
    fun hasAccessToAllNamespaces(keycloakPrincipal: KeycloakPrincipal): Boolean

    fun hasAccessToNamespace(
        keycloakPrincipal: KeycloakPrincipal,
        namespaceId: Long,
    ): Boolean

    fun hasAccessToApplication(
        keycloakPrincipal: KeycloakPrincipal,
        namespaceId: Long,
        applicationId: Long,
    ): Boolean

    fun hasAccessToConfigurations(
        keycloakPrincipal: KeycloakPrincipal,
        namespaceId: Long,
        applicationId: Long,
    ): Boolean
}
