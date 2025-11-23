package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.repository.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.service.dto.Application
import ru.enzhine.rtcms4j.core.service.dto.Configuration
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.dto.Namespace

fun ConfigurationDetailed.toUndetailed() =
    Configuration(
        id = id,
        namespaceId = namespaceId,
        applicationId = applicationId,
        name = name,
        commitHash = commitHash,
        streamKey = streamKey,
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
