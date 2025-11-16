package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.service.dto.Configuration
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.dto.ConfigurationDetailed

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
