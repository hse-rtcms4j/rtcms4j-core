package ru.enzhine.rtcms4j.core.api.event

data class NotificationEventDto(
    val namespaceId: Long,
    val applicationId: Long,
    val secretRotatedEvent: SecretRotatedEventDto?,
    val configurationUpdatedEvent: ConfigurationUpdatedEventDto?,
) {
    data class SecretRotatedEventDto(
        val newSecret: String,
    )

    data class ConfigurationUpdatedEventDto(
        val configurationId: Long,
        val payload: String,
    )
}
