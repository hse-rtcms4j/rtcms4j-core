package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType as ServiceSourceType

fun ServiceSourceType.toRepository() =
    when (this) {
        ServiceSourceType.SERVICE -> SourceType.SERVICE
        ServiceSourceType.USER -> SourceType.USER
    }

fun ConfigSchemaDetailedEntity.toUndetailed() =
    ConfigSchemaEntity(
        id = id,
        createdAt = createdAt,
        configurationId = configurationId,
        sourceType = sourceType,
        sourceIdentity = sourceIdentity,
    )

fun ConfigCommitDetailedEntity.toUndetailed() =
    ConfigCommitEntity(
        id = id,
        createdAt = createdAt,
        configSchemaId = configSchemaId,
        configurationId = configurationId,
        sourceType = sourceType,
        sourceIdentity = sourceIdentity,
    )
