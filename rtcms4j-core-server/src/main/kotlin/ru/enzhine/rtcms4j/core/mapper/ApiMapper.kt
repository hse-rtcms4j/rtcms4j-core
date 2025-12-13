package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.api.dto.ApplicationDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDto
import ru.enzhine.rtcms4j.core.api.dto.KeycloakClientDto
import ru.enzhine.rtcms4j.core.api.dto.NamespaceDto
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.KeycloakClient
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType
import ru.enzhine.rtcms4j.core.api.dto.SourceType as ApiSourceType

fun Namespace.toApi() =
    NamespaceDto(
        id = id,
        name = name,
        description = description,
    )

fun Application.toApi() =
    ApplicationDto(
        id = id,
        namespaceId = namespaceId,
        name = name,
        description = description,
    )

fun KeycloakClient.toApi() =
    KeycloakClientDto(
        clientId = clientId,
        secret = clientSecret,
    )

fun Configuration.toApi() =
    ConfigurationDto(
        id = id,
        namespaceId = namespaceId,
        applicationId = applicationId,
        name = name,
        schemaSourceType = schemaSourceType.toApi(),
        commitId = actualCommitId,
    )

fun ConfigurationDetailed.toApi() =
    ConfigurationDetailedDto(
        id = id,
        namespaceId = namespaceId,
        applicationId = applicationId,
        name = name,
        schemaSourceType = schemaSourceType.toApi(),
        commitId = actualCommitId,
        jsonSchema = jsonSchema,
        jsonValues = jsonValues,
    )

fun SourceType.toApi() =
    when (this) {
        SourceType.SERVICE -> ApiSourceType.SERVICE
        SourceType.USER -> ApiSourceType.USER
    }

fun ApiSourceType.toService() =
    when (this) {
        ApiSourceType.SERVICE -> SourceType.SERVICE
        ApiSourceType.USER -> SourceType.USER
    }

fun ConfigurationCommit.toApi() =
    ConfigurationCommitDto(
        namespaceId = namespaceId,
        applicationId = applicationId,
        configurationId = configurationId,
        commitId = id,
        sourceType = sourceType.toApi(),
        sourceIdentity = sourceIdentity,
    )

fun ConfigurationCommitDetailed.toApi() =
    ConfigurationCommitDetailedDto(
        namespaceId = namespaceId,
        applicationId = applicationId,
        configurationId = configurationId,
        commitId = id,
        sourceType = sourceType.toApi(),
        sourceIdentity = sourceIdentity,
        jsonSchema = jsonSchema,
        jsonValues = jsonValues,
    )
