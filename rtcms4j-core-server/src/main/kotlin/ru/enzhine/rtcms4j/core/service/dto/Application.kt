package ru.enzhine.rtcms4j.core.service.dto

data class Application(
    val id: Long,
    val namespaceId: Long,
    var name: String,
    var description: String
)
