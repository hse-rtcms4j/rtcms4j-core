package ru.enzhine.rtcms4j.core.repository.kv.dto

data class NotifyEventDto(
    val namespaceId: Long,
    val applicationId: Long,
    val passwordRotatedEvent: PasswordRotatedEvent?,
    val configUpdatedEvent: ConfigUpdatedEvent?,
) {
    data class PasswordRotatedEvent(
        val newSecret: String,
    )

    data class ConfigUpdatedEvent(
        val configurationId: Long,
        val payload: String,
    )
}
