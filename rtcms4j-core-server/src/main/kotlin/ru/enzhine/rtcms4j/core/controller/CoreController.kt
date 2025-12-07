package ru.enzhine.rtcms4j.core.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.core.api.dto.ApplicationCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.ApplicationDto
import ru.enzhine.rtcms4j.core.api.dto.ApplicationUpdateRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDtoCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDtoUpdateRequest
import ru.enzhine.rtcms4j.core.api.dto.KeycloakClientDto
import ru.enzhine.rtcms4j.core.api.dto.NamespaceCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.NamespaceDto
import ru.enzhine.rtcms4j.core.api.dto.NamespaceUpdateRequest
import ru.enzhine.rtcms4j.core.api.dto.UserInfoDto
import ru.enzhine.rtcms4j.core.builder.applicationNotFoundException
import ru.enzhine.rtcms4j.core.builder.configurationNotFoundException
import ru.enzhine.rtcms4j.core.builder.namespaceNotFoundException
import ru.enzhine.rtcms4j.core.mapper.toApi
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.service.internal.ApplicationService
import ru.enzhine.rtcms4j.core.service.internal.ConfigurationService
import ru.enzhine.rtcms4j.core.service.internal.NamespaceService
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType
import java.util.UUID

@RestController
class CoreController(
    private val namespaceService: NamespaceService,
    private val applicationService: ApplicationService,
    private val configurationService: ConfigurationService,
) : CoreApi {
    override fun createNamespace(namespaceCreateRequest: NamespaceCreateRequest): ResponseEntity<NamespaceDto> {
        val assigner = UUID.randomUUID() // TODO

        val responseBody =
            namespaceService
                .createNamespace(
                    creator = assigner,
                    name = namespaceCreateRequest.name,
                    description = namespaceCreateRequest.description,
                ).toApi()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(responseBody)
    }

    override fun updateNamespace(
        nid: Long,
        namespaceUpdateRequest: NamespaceUpdateRequest,
    ): ResponseEntity<NamespaceDto> {
        val responseBody =
            namespaceService
                .updateNamespace(
                    namespaceId = nid,
                    name = namespaceUpdateRequest.name,
                    description = namespaceUpdateRequest.description,
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun getNamespace(nid: Long): ResponseEntity<NamespaceDto> {
        val responseBody =
            namespaceService
                .getNamespaceById(
                    namespaceId = nid,
                    forShare = false,
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun findAllNamespaces(
        name: String?,
        pageable: Pageable?,
    ): ResponseEntity<PagedModel<NamespaceDto>> {
        val responseBody =
            PagedModel(
                namespaceService
                    .findNamespaces(
                        name = name,
                        pageable = pageable,
                    ).map { it.toApi() },
            )

        return ResponseEntity
            .ok(responseBody)
    }

    override fun deleteNamespace(nid: Long): ResponseEntity<Unit> {
        if (namespaceService.deleteNamespace(nid)) {
            return ResponseEntity
                .noContent()
                .build()
        }

        throw namespaceNotFoundException(nid)
    }

    override fun addNamespaceAdmin(
        nid: Long,
        uid: UUID,
    ): ResponseEntity<UserInfoDto> {
        val assigner = UUID.randomUUID() // TODO

        if (
            namespaceService.addAdmin(
                assigner = assigner,
                namespaceId = nid,
                sub = uid,
            )
        ) {
            val responseBody =
                UserInfoDto(sub = uid)

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody)
        }

        return ResponseEntity
            .badRequest()
            .build()
    }

    override fun getNamespaceAdmins(nid: Long): ResponseEntity<List<UserInfoDto>> {
        val responseBody =
            namespaceService
                .listAdmins(nid)
                .map { uid ->
                    UserInfoDto(sub = uid)
                }

        return ResponseEntity.ok(responseBody)
    }

    override fun removeNamespaceAdmin(
        nid: Long,
        uid: UUID,
    ): ResponseEntity<Unit> {
        if (
            namespaceService.removeAdmin(
                namespaceId = nid,
                sub = uid,
            )
        ) {
            return ResponseEntity
                .noContent()
                .build()
        }

        return ResponseEntity
            .notFound()
            .build()
    }

    override fun createApplication(
        nid: Long,
        applicationCreateRequest: ApplicationCreateRequest,
    ): ResponseEntity<ApplicationDto> {
        val assigner = UUID.randomUUID() // TODO

        val responseBody =
            applicationService
                .createApplication(
                    creator = assigner,
                    namespaceId = nid,
                    name = applicationCreateRequest.name,
                    description = applicationCreateRequest.description,
                ).toApi()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(responseBody)
    }

    override fun updateApplication(
        nid: Long,
        aid: Long,
        applicationUpdateRequest: ApplicationUpdateRequest,
    ): ResponseEntity<ApplicationDto> {
        val responseBody =
            applicationService
                .updateApplication(
                    namespaceId = nid,
                    applicationId = aid,
                    name = applicationUpdateRequest.name,
                    description = applicationUpdateRequest.description,
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun getApplication(
        nid: Long,
        aid: Long,
    ): ResponseEntity<ApplicationDto> {
        val responseBody =
            applicationService
                .getApplicationById(
                    namespaceId = nid,
                    applicationId = aid,
                    forShare = false,
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun findAllApplications(
        nid: Long,
        name: String?,
        pageable: Pageable?,
    ): ResponseEntity<PagedModel<ApplicationDto>> {
        val responseBody =
            PagedModel(
                applicationService
                    .findApplications(
                        namespaceId = nid,
                        name = name,
                        pageable = pageable,
                    ).map { it.toApi() },
            )

        return ResponseEntity
            .ok(responseBody)
    }

    override fun deleteApplication(
        nid: Long,
        aid: Long,
    ): ResponseEntity<Unit> {
        if (
            applicationService.deleteApplication(
                namespaceId = nid,
                applicationId = aid,
            )
        ) {
            return ResponseEntity
                .noContent()
                .build()
        }

        throw applicationNotFoundException(aid)
    }

    override fun addApplicationManager(
        nid: Long,
        aid: Long,
        uid: UUID,
    ): ResponseEntity<UserInfoDto> {
        val assigner = UUID.randomUUID() // TODO

        if (
            applicationService.addManager(
                assigner = assigner,
                namespaceId = nid,
                applicationId = aid,
                sub = uid,
            )
        ) {
            val responseBody =
                UserInfoDto(sub = uid)

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody)
        }

        return ResponseEntity
            .badRequest()
            .build()
    }

    override fun getApplicationManagers(
        nid: Long,
        aid: Long,
    ): ResponseEntity<List<UserInfoDto>> {
        val responseBody =
            applicationService
                .listManagers(
                    namespaceId = nid,
                    applicationId = aid,
                ).map { uid ->
                    UserInfoDto(sub = uid)
                }

        return ResponseEntity.ok(responseBody)
    }

    override fun removeApplicationManager(
        nid: Long,
        aid: Long,
        uid: UUID,
    ): ResponseEntity<Unit> {
        if (
            applicationService.removeManager(
                namespaceId = nid,
                applicationId = aid,
                sub = uid,
            )
        ) {
            return ResponseEntity
                .noContent()
                .build()
        }

        return ResponseEntity
            .notFound()
            .build()
    }

    override fun getApplicationClient(
        nid: Long,
        aid: Long,
    ): ResponseEntity<KeycloakClientDto> {
        val responseBody =
            applicationService
                .getApplicationClientCredentials(
                    namespaceId = nid,
                    applicationId = aid,
                ).toApi()

        return ResponseEntity.ok(responseBody)
    }

    override fun rotateApplicationClientPassword(
        nid: Long,
        aid: Long,
    ): ResponseEntity<KeycloakClientDto> {
        val responseBody =
            applicationService
                .rotateApplicationClientCredentials(
                    namespaceId = nid,
                    applicationId = aid,
                ).toApi()

        return ResponseEntity.ok(responseBody)
    }

    override fun createConfiguration(
        nid: Long,
        aid: Long,
        configurationDtoCreateRequest: ConfigurationDtoCreateRequest,
    ): ResponseEntity<ConfigurationDetailedDto> {
        val assigner = UUID.randomUUID() // TODO

        val responseBody =
            configurationService
                .createConfiguration(
                    creator = assigner,
                    namespaceId = nid,
                    applicationId = aid,
                    name = configurationDtoCreateRequest.name,
                    schemaSourceType = configurationDtoCreateRequest.schemaSourceType.toService(),
                ).toApi()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(responseBody)
    }

    override fun updateConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
        configurationDtoUpdateRequest: ConfigurationDtoUpdateRequest,
    ): ResponseEntity<ConfigurationDto> {
        val responseBody =
            configurationService
                .updateConfiguration(
                    namespaceId = nid,
                    applicationId = aid,
                    configurationId = cid,
                    name = configurationDtoUpdateRequest.name,
                    schemaSourceType = configurationDtoUpdateRequest.schemaSourceType?.toService(),
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun getConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
    ): ResponseEntity<ConfigurationDetailedDto> {
        val responseBody =
            configurationService
                .getConfigurationById(
                    namespaceId = nid,
                    applicationId = aid,
                    configurationId = cid,
                    forShare = false,
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun findAllConfigurations(
        nid: Long,
        aid: Long,
        name: String?,
        pageable: Pageable?,
    ): ResponseEntity<PagedModel<ConfigurationDto>> {
        val responseBody =
            PagedModel(
                configurationService
                    .findConfigurations(
                        namespaceId = nid,
                        applicationId = aid,
                        name = name,
                        pageable = pageable,
                    ).map { it.toApi() },
            )

        return ResponseEntity
            .ok(responseBody)
    }

    override fun deleteConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
    ): ResponseEntity<Unit> {
        if (
            configurationService.deleteConfiguration(
                namespaceId = nid,
                applicationId = aid,
                configurationId = cid,
            )
        ) {
            return ResponseEntity
                .noContent()
                .build()
        }

        throw configurationNotFoundException(cid)
    }

    override fun applyConfigurationCommit(
        nid: Long,
        aid: Long,
        cid: Long,
        ctid: Long,
    ): ResponseEntity<Unit> {
        configurationService.applyConfigurationByCommitId(
            namespaceId = nid,
            applicationId = aid,
            configurationId = cid,
            commitId = ctid,
        )

        return ResponseEntity
            .noContent()
            .build()
    }

    override fun commitConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
        configurationCommitRequest: ConfigurationCommitRequest,
    ): ResponseEntity<ConfigurationCommitDetailedDto> {
        val sourceType = SourceType.USER // TODO
        val sourceIdentity = UUID.randomUUID().toString() // TODO

        val responseBody =
            configurationService
                .commitValuesAndSchema(
                    namespaceId = nid,
                    applicationId = aid,
                    configurationId = cid,
                    sourceType = sourceType,
                    sourceIdentity = sourceIdentity,
                    jsonSchema = configurationCommitRequest.jsonSchema,
                    jsonValues = configurationCommitRequest.jsonValues,
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun getConfigurationCommit(
        nid: Long,
        aid: Long,
        cid: Long,
        ctid: Long,
    ): ResponseEntity<ConfigurationCommitDetailedDto> {
        val responseBody =
            configurationService
                .getConfigurationCommitByCommitId(
                    namespaceId = nid,
                    applicationId = aid,
                    configurationId = cid,
                    forShare = false,
                    commitId = ctid,
                ).toApi()

        return ResponseEntity
            .ok(responseBody)
    }

    override fun getConfigurationCommits(
        nid: Long,
        aid: Long,
        cid: Long,
    ): ResponseEntity<List<ConfigurationCommitDto>> {
        val responseBody =
            configurationService
                .getConfigurationCommits(
                    namespaceId = nid,
                    applicationId = aid,
                    configurationId = cid,
                ).map {
                    it.toApi()
                }

        return ResponseEntity
            .ok(responseBody)
    }
}
