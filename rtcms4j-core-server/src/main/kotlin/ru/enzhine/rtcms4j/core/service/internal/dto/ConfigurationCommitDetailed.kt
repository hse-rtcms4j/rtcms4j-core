package ru.enzhine.rtcms4j.core.service.internal.dto

data class ConfigurationCommitDetailed(
    val id: Long,
    val namespaceId: Long,
    val applicationId: Long,
    val configurationId: Long,
    val sourceType: SourceType,
    val sourceIdentity: String,
    val commitHash: String,
    val valuesData: String?,
    val schemaData: String?,
)
