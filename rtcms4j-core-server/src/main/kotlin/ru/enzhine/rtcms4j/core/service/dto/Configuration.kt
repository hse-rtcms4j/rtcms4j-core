package ru.enzhine.rtcms4j.core.service.dto

import ru.enzhine.rtcms4j.core.repository.dto.SourceType

data class Configuration(
    val id: Long,
    val namespaceId: Long,
    val applicationId: Long,
    var name: String,
    val commitHash: String?,
    var streamKey: String?,
    var schemaSourceType: SourceType,
)
