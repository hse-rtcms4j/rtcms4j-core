package ru.enzhine.rtcms4j.core.repository.kv.dto

data class CacheKey(
    val namespaceId: Long,
    val applicationId: Long,
    val configurationId: Long,
)
