package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.service.dto.Application
import ru.enzhine.rtcms4j.core.service.dto.Configuration
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.dto.Namespace
import ru.enzhine.rtcms4j.core.service.dto.SourceType
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType as RepositorySourceType

fun RepositorySourceType.toService() =
    when (this) {
        RepositorySourceType.SERVICE -> SourceType.SERVICE
        RepositorySourceType.USER -> SourceType.USER
    }

fun ConfigurationDetailed.toUndetailed() =
    Configuration(
        id = id,
        namespaceId = namespaceId,
        applicationId = applicationId,
        name = name,
        commitHash = commitHash,
        schemaSourceType = schemaSourceType,
    )

fun ConfigurationCommitDetailed.toUndetailed() =
    ConfigurationCommit(
        id = id,
        namespaceId = namespaceId,
        applicationId = applicationId,
        configurationId = configurationId,
        sourceType = sourceType,
        sourceIdentity = sourceIdentity,
        commitHash = commitHash,
    )

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
        accessToken = accessToken,
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
