package ru.enzhine.rtcms4j.core.service.internal.dto

import java.time.OffsetDateTime

data class ConfigurationCommit(
    val createdAt: OffsetDateTime,
    val id: Long,
    val schemaId: Long,
    val namespaceId: Long,
    val applicationId: Long,
    val configurationId: Long,
    val sourceType: SourceType,
    val sourceIdentity: String,
    val version: String,
)
