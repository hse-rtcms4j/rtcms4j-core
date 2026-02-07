package ru.enzhine.rtcms4j.core.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "key-val-repository")
data class KeyValRepositoryProperties(
    val globalPrefix: String,
    val topic: String,
)
