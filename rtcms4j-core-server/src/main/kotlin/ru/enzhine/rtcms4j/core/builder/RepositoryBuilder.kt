package ru.enzhine.rtcms4j.core.builder

import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationManagerEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceAdminEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType
import java.time.OffsetDateTime
import java.util.UUID

fun newNamespaceEntity(
    creatorSub: UUID,
    name: String,
    description: String,
) = NamespaceEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    updatedAt = OffsetDateTime.MIN,
    creatorSub = creatorSub,
    name = name,
    description = description,
)

fun newNamespaceAdminEntity(
    namespaceId: Long,
    assignerSub: UUID,
    userSub: UUID,
) = NamespaceAdminEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    updatedAt = OffsetDateTime.MIN,
    namespaceId = namespaceId,
    assignerSub = assignerSub,
    userSub = userSub,
)

fun newApplicationEntity(
    namespaceId: Long,
    creatorSub: UUID,
    name: String,
    description: String,
    accessToken: String,
) = ApplicationEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    updatedAt = OffsetDateTime.MIN,
    namespaceId = namespaceId,
    creatorSub = creatorSub,
    name = name,
    description = description,
    accessToken = accessToken,
)

fun newApplicationManagerEntity(
    applicationId: Long,
    assignerSub: UUID,
    userSub: UUID,
) = ApplicationManagerEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    updatedAt = OffsetDateTime.MIN,
    applicationId = applicationId,
    assignerSub = assignerSub,
    userSub = userSub,
)

fun newConfigurationEntity(
    applicationId: Long,
    creatorSub: UUID,
    name: String,
    schemaSourceType: SourceType,
    usedCommitHash: String?,
) = ConfigurationEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    updatedAt = OffsetDateTime.MIN,
    applicationId = applicationId,
    creatorSub = creatorSub,
    name = name,
    schemaSourceType = schemaSourceType,
    commitHash = usedCommitHash,
)

fun newConfigurationCommitEntity(
    configurationId: Long,
    sourceType: SourceType,
    sourceIdentity: String,
    commitHash: String,
    jsonValues: String?,
    jsonSchema: String?,
) = ConfigurationCommitDetailedEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    configurationId = configurationId,
    sourceType = sourceType,
    sourceIdentity = sourceIdentity,
    commitHash = commitHash,
    jsonValues = jsonValues,
    jsonSchema = jsonSchema,
)
