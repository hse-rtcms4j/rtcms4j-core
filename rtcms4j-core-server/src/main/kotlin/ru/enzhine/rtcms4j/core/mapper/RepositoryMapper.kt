package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitEntity

fun ConfigurationCommitDetailedEntity.toUndetailed() =
    ConfigurationCommitEntity(
        id = id,
        createdAt = createdAt,
        configurationId = configurationId,
        sourceType = sourceType,
        sourceIdentity = sourceIdentity,
        commitHash = commitHash,
    )
