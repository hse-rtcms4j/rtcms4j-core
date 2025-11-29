package ru.enzhine.rtcms4j.core.service.dto

import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType

data class ConfigurationCommit(
    val id: Long,
    val namespaceId: Long,
    val applicationId: Long,
    val configurationId: Long,
    val sourceType: SourceType,
    val sourceIdentity: String,
    val commitHash: String,
)
