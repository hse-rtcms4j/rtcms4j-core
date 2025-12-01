package ru.enzhine.rtcms4j.core.repository.kv.dto

data class NotifyEventDto(
    val namespaceId: Long,
    val applicationId: Long,
    val eventType: EventType,
    val content: String,
)
