package ru.enzhine.rtcms4j.core.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak")
data class KeycloakProperties(
    val serverUrl: String,
    val realm: String,
    val admin: AdminProperties,
    val retriesLimit: Int,
) {
    data class AdminProperties(
        val clientId: String,
        val clientSecret: String,
    )
}
