package ru.enzhine.rtcms4j.core.service.internal.dto

data class Application(
    val id: Long,
    val namespaceId: Long,
    val name: String,
    val description: String,
)
