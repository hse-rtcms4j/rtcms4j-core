package ru.enzhine.rtcms4j.core.service.internal.dto

data class Configuration(
    val id: Long,
    val namespaceId: Long,
    val applicationId: Long,
    val name: String,
    val schemaSourceType: SourceType,
    val actualCommitId: Long?,
    val actualCommitVersion: String?,
)
