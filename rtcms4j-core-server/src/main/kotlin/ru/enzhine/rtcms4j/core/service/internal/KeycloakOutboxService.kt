package ru.enzhine.rtcms4j.core.service.internal

interface KeycloakOutboxService {
    fun createKeycloakApplicationGuaranteed(
        namespaceId: Long,
        applicationId: Long,
    )

    fun deleteKeycloakApplicationGuaranteed(
        namespaceId: Long,
        applicationId: Long,
    )
}
