package ru.enzhine.rtcms4j.core.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "cache")
data class CacheProperties(
    val keycloakUsersTtl: Duration,
    val availableResourcesTtl: Duration,
)
