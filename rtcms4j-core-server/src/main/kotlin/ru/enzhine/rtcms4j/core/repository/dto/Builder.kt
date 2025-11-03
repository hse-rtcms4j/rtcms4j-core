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
