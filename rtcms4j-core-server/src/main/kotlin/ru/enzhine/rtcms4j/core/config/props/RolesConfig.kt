package ru.enzhine.rtcms4j.core.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "roles")
data class RolesConfig(
    val superAdminRole: String,
)
