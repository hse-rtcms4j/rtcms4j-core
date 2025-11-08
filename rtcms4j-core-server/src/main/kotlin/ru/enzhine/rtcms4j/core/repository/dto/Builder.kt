package ru.enzhine.rtcms4j.core.repository.dto

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
