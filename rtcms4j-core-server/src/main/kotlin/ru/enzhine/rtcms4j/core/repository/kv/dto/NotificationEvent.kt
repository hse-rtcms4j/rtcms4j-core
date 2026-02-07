package ru.enzhine.rtcms4j.core.repository.kv.dto

data class NotificationEvent(
    val namespaceId: Long,
    val applicationId: Long,
    val secretRotatedEvent: SecretRotatedEvent?,
    val configUpdatedEvent: ConfigUpdatedEvent?,
) {
    data class SecretRotatedEvent(
        val newSecret: String,
    )

    data class ConfigUpdatedEvent(
        val configurationId: Long,
        val payload: String,
    )
}
