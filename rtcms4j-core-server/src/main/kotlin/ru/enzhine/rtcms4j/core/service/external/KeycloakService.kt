package ru.enzhine.rtcms4j.core.service.external

import ru.enzhine.rtcms4j.core.service.external.dto.KeycloakClient

interface KeycloakService {
    fun buildClientId(
        namespaceId: Long,
        applicationId: Long,
    ): String

    fun findApplicationClient(clientId: String): KeycloakClient

    fun createNewApplicationClient(clientId: String): KeycloakClient

    fun rotateApplicationClientPassword(clientId: String): KeycloakClient

    fun deleteApplicationClient(clientId: String): Boolean
}
