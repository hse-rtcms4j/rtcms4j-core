package ru.enzhine.rtcms4j.core.service.external.dto

import java.util.UUID

data class KeycloakClient(
    val sub: UUID,
    val clientId: String,
    val clientSecret: String,
)
