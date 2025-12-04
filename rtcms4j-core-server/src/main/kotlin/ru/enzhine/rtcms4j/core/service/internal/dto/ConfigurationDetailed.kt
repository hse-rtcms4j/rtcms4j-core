package ru.enzhine.rtcms4j.core.service.internal.dto

data class ConfigurationDetailed(
    val id: Long,
    val namespaceId: Long,
    val applicationId: Long,
    val name: String,
    val schemaSourceType: SourceType,
    val commitId: Long?,
    val jsonSchema: String?,
    val jsonValues: String?,
)
