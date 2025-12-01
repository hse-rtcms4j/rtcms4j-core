package ru.enzhine.rtcms4j.core.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak.admin")
data class KeycloakAdminProperties(
    val serverUrl: String,
    val realm: String,
    val clientId: String,
    val username: String,
    val password: String,
    val retriesLimit: Int,
)
