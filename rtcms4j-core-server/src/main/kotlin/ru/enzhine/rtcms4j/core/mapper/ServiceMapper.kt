package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationCommitEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType as RepositorySourceType

fun RepositorySourceType.toService() =
    when (this) {
        RepositorySourceType.SERVICE -> SourceType.SERVICE
        RepositorySourceType.USER -> SourceType.USER
    }

fun NamespaceEntity.toService() =
    Namespace(
        id = id,
        name = name,
        description = description,
    )

fun ApplicationEntity.toService() =
    Application(
        id = id,
        namespaceId = namespaceId,
        name = name,
        description = description,
    )

fun ConfigurationEntity.toService(namespaceId: Long) =
    Configuration(
        id = id,
        namespaceId = namespaceId,
        applicationId = applicationId,
        name = name,
        commitHash = commitHash,
        schemaSourceType = schemaSourceType.toService(),
    )

fun ConfigurationCommitEntity.toService(
    namespaceId: Long,
    applicationId: Long,
) = ConfigurationCommit(
    id = id,
    namespaceId = namespaceId,
    applicationId = applicationId,
    configurationId = configurationId,
    sourceType = sourceType.toService(),
    sourceIdentity = sourceIdentity,
    commitHash = commitHash,
)

fun ConfigurationCommitDetailedEntity.toService(
    namespaceId: Long,
    applicationId: Long,
) = ConfigurationCommitDetailed(
    id = id,
    namespaceId = namespaceId,
    applicationId = applicationId,
    configurationId = configurationId,
    sourceType = sourceType.toService(),
    sourceIdentity = sourceIdentity,
    commitHash = commitHash,
    valuesData = jsonValues,
    schemaData = jsonSchema,
)

fun Configuration.toDetailed(
    valuesData: String?,
    schemaData: String?,
) = ConfigurationDetailed(
    id = id,
    namespaceId = namespaceId,
    applicationId = applicationId,
    name = name,
    commitHash = commitHash,
    schemaSourceType = schemaSourceType,
    valuesData = valuesData,
    schemaData = schemaData,
)
