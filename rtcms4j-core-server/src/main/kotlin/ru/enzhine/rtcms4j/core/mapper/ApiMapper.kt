package ru.enzhine.rtcms4j.core.mapper

import ru.enzhine.rtcms4j.core.api.dto.ApplicationDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDto
import ru.enzhine.rtcms4j.core.api.dto.KeycloakClientDto
import ru.enzhine.rtcms4j.core.api.dto.NamespaceDto
import ru.enzhine.rtcms4j.core.api.dto.UserRoleDto
import ru.enzhine.rtcms4j.core.api.event.NotificationEventDto
import ru.enzhine.rtcms4j.core.repository.kv.dto.NotificationEvent
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommit
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationCommitDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.ConfigurationDetailed
import ru.enzhine.rtcms4j.core.service.internal.dto.KeycloakClient
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType
import ru.enzhine.rtcms4j.core.service.internal.dto.UserRole
import ru.enzhine.rtcms4j.core.api.dto.SourceType as ApiSourceType

fun Namespace.toApi() =
    NamespaceDto(
        id,
        name,
        description,
    )

fun Application.toApi() =
    ApplicationDto(
        id,
        namespaceId,
        name,
        description,
        creationByService,
    )

fun KeycloakClient.toApi() =
    KeycloakClientDto(
        clientId,
        clientSecret,
    )

fun Configuration.toApi() =
    ConfigurationDto(
        id,
        namespaceId,
        applicationId,
        name,
        schemaSourceType.toApi(),
    ).also { api ->
        api.commitId = actualCommitId
        api.commitVersion = actualCommitVersion
    }

fun ConfigurationDetailed.toApi() =
    ConfigurationDetailedDto(
        id,
        namespaceId,
        applicationId,
        name,
        schemaSourceType.toApi(),
    ).also { api ->
        api.commitId = actualCommitId
        api.commitVersion = actualCommitVersion
        api.jsonSchema = jsonSchema
        api.jsonValues = jsonValues
    }

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
        namespaceId,
        applicationId,
        configurationId,
        id,
        version,
        sourceType.toApi(),
        sourceIdentity,
    )

fun ConfigurationCommitDetailed.toApi() =
    ConfigurationCommitDetailedDto(
        namespaceId,
        applicationId,
        configurationId,
        id,
        version,
        sourceType.toApi(),
        sourceIdentity,
        jsonSchema,
        jsonValues,
    )

fun UserRole.toApi() =
    UserRoleDto(
        subject,
        assignerSubject,
    ).also { api ->
        api.username = username
        api.firstName = firstName
        api.lastName = lastName
    }

fun NotificationEvent.toApi() =
    NotificationEventDto(
        namespaceId = namespaceId,
        applicationId = applicationId,
        secretRotatedEvent = secretRotatedEvent?.toApi(),
        configurationUpdatedEvent = configUpdatedEvent?.toApi(),
    )

fun NotificationEvent.ConfigUpdatedEvent.toApi() =
    NotificationEventDto.ConfigurationUpdatedEventDto(
        configurationId = configurationId,
        payload = payload,
    )

fun NotificationEvent.SecretRotatedEvent.toApi() =
    NotificationEventDto.SecretRotatedEventDto(
        newSecret = newSecret,
    )
