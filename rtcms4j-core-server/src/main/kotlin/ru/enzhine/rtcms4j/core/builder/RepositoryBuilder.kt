package ru.enzhine.rtcms4j.core.builder

import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationManagerEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaDetailedEntity
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
    creationByService: Boolean,
) = ApplicationEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    updatedAt = OffsetDateTime.MIN,
    namespaceId = namespaceId,
    creatorSub = creatorSub,
    name = name,
    description = description,
    creationByService = creationByService,
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
    actualCommitId: Long?,
    actualCommitVersion: String?,
) = ConfigurationEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    updatedAt = OffsetDateTime.MIN,
    applicationId = applicationId,
    creatorSub = creatorSub,
    name = name,
    schemaSourceType = schemaSourceType,
    actualCommitId = actualCommitId,
    actualCommitVersion = actualCommitVersion,
)

fun newConfigSchemaDetailedEntity(
    configurationId: Long,
    sourceType: SourceType,
    sourceIdentity: String,
    jsonSchema: String,
) = ConfigSchemaDetailedEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    configurationId = configurationId,
    sourceType = sourceType,
    sourceIdentity = sourceIdentity,
    jsonSchema = jsonSchema,
)

fun newConfigCommitDetailedEntity(
    configSchemaId: Long,
    configurationId: Long,
    sourceType: SourceType,
    sourceIdentity: String,
    jsonValues: String,
) = ConfigCommitDetailedEntity(
    id = 0L,
    createdAt = OffsetDateTime.MIN,
    configSchemaId = configSchemaId,
    configurationId = configurationId,
    sourceType = sourceType,
    sourceIdentity = sourceIdentity,
    version = "",
    jsonValues = jsonValues,
)
