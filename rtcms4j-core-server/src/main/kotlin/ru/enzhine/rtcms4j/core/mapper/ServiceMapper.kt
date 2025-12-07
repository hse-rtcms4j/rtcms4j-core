package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.service.external.dto.ApplicationClient
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.ClientCredentials
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
        schemaSourceType = schemaSourceType.toService(),
        actualCommitId = actualCommitId,
    )

fun ConfigCommitEntity.toService(
    namespaceId: Long,
    applicationId: Long,
    configurationId: Long,
) = ConfigurationCommit(
    id = id,
    namespaceId = namespaceId,
    applicationId = applicationId,
    configurationId = configurationId,
    sourceType = sourceType.toService(),
    sourceIdentity = sourceIdentity,
)

fun ConfigCommitDetailedEntity.toService(
    namespaceId: Long,
    applicationId: Long,
    configurationId: Long,
    jsonSchema: String,
) = ConfigurationCommitDetailed(
    id = id,
    namespaceId = namespaceId,
    applicationId = applicationId,
    configurationId = configurationId,
    sourceType = sourceType.toService(),
    sourceIdentity = sourceIdentity,
    jsonSchema = jsonSchema,
    jsonValues = jsonValues,
)

fun Configuration.toDetailed(
    jsonSchema: String?,
    jsonValues: String?,
) = ConfigurationDetailed(
    id = id,
    namespaceId = namespaceId,
    applicationId = applicationId,
    name = name,
    schemaSourceType = schemaSourceType,
    actualCommitId = actualCommitId,
    jsonSchema = jsonSchema,
    jsonValues = jsonValues,
)

fun ApplicationClient.toClientCredentials() =
    ClientCredentials(
        clientId = clientId,
        clientSecret = clientSecret,
    )
