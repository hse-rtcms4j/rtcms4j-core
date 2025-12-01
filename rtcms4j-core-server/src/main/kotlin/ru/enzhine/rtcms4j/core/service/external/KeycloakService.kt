package ru.enzhine.rtcms4j.core.service.external

import ru.enzhine.rtcms4j.core.service.external.dto.ApplicationClient

interface KeycloakService {
    fun buildClientId(
        namespaceId: Long,
        applicationId: Long,
    ): String

    fun findApplicationClient(clientId: String): ApplicationClient

    fun createNewApplicationClient(clientId: String): ApplicationClient

    fun rotateApplicationClientPassword(clientId: String): ApplicationClient
}
