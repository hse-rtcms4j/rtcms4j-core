package ru.enzhine.rtcms4j.core.service.external

import ru.enzhine.rtcms4j.core.service.external.dto.KeycloakClient
import ru.enzhine.rtcms4j.core.service.external.dto.KeycloakUser
import java.util.UUID

interface KeycloakService {
    fun buildClientId(
        namespaceId: Long,
        applicationId: Long,
    ): String

    fun isUserExists(subject: UUID): Boolean

    fun getUserOrCache(subject: UUID): KeycloakUser?

    fun findApplicationClient(
        namespaceId: Long,
        applicationId: Long,
    ): KeycloakClient

    fun createNewApplicationClient(
        namespaceId: Long,
        applicationId: Long,
    ): KeycloakClient

    fun rotateApplicationClientPassword(
        namespaceId: Long,
        applicationId: Long,
    ): KeycloakClient

    fun deleteApplicationClient(
        namespaceId: Long,
        applicationId: Long,
    )
}
