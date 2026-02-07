package ru.enzhine.rtcms4j.core.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "default.pagination")
data class DefaultPaginationProperties(
    val pageSize: Int,
)
