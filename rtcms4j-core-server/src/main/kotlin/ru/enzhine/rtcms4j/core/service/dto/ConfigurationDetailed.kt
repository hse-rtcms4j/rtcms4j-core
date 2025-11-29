package ru.enzhine.rtcms4j.core.service.dto

data class ConfigurationDetailed(
    val id: Long,
    val namespaceId: Long,
    val applicationId: Long,
    val name: String,
    val schemaSourceType: SourceType,
    val commitHash: String?,
    val valuesData: String?,
    val schemaData: String?,
)
