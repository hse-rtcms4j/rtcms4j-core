package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationCommitEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType
import ru.enzhine.rtcms4j.core.service.dto.SourceType as ServiceSourceType

fun ServiceSourceType.toRepository() =
    when (this) {
        ServiceSourceType.SERVICE -> SourceType.SERVICE
        ServiceSourceType.USER -> SourceType.USER
    }

fun ConfigurationCommitDetailedEntity.toUndetailed() =
    ConfigurationCommitEntity(
        id = id,
        createdAt = createdAt,
        configurationId = configurationId,
        sourceType = sourceType,
        sourceIdentity = sourceIdentity,
        commitHash = commitHash,
    )
