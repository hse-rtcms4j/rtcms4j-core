package ru.enzhine.rtcms4j.core.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.http.ResponseEntity
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.core.api.dto.AccessTokenDto
import ru.enzhine.rtcms4j.core.api.dto.ApplicationCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.ApplicationDto
import ru.enzhine.rtcms4j.core.api.dto.ApplicationUpdateRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationAcknowledge
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDtoCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDtoUpdateRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationStreamState
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationVersion
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationVersionWithState
import ru.enzhine.rtcms4j.core.api.dto.NamespaceCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.NamespaceDto
import ru.enzhine.rtcms4j.core.api.dto.NamespaceUpdateRequest
import ru.enzhine.rtcms4j.core.api.dto.UserInfoDto
import java.util.UUID

class CoreController : CoreApi {
    override fun acknowledgeConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
        configurationAcknowledge: ConfigurationAcknowledge,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun addApplicationManager(
        nid: Long,
        aid: Long,
        uid: UUID,
    ): ResponseEntity<UserInfoDto> {
        TODO("Not yet implemented")
    }

    override fun addNamespaceAdmin(
        nid: Long,
        uid: UUID,
    ): ResponseEntity<UserInfoDto> {
        TODO("Not yet implemented")
    }

    override fun createApplication(
        nid: Long,
        applicationCreateRequest: ApplicationCreateRequest,
    ): ResponseEntity<ApplicationDto> {
        TODO("Not yet implemented")
    }

    override fun createConfiguration(
        nid: Long,
        aid: Long,
        configurationDtoCreateRequest: ConfigurationDtoCreateRequest,
    ): ResponseEntity<ConfigurationDto> {
        TODO("Not yet implemented")
    }

    override fun createNamespace(namespaceCreateRequest: NamespaceCreateRequest): ResponseEntity<NamespaceDto> {
        TODO("Not yet implemented")
    }

    override fun deleteApplication(
        nid: Long,
        aid: Long,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteConfigurationVersion(
        nid: Long,
        aid: Long,
        cid: Long,
        commitHash: String,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteNamespace(nid: Long): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun findAllApplications(
        nid: Long,
        name: String?,
        pageable: Pageable?,
    ): ResponseEntity<PagedModel<ApplicationDto>> {
        TODO("Not yet implemented")
    }

    override fun findAllConfigurations(
        nid: Long,
        aid: Long,
        name: String?,
        pageable: Pageable?,
    ): ResponseEntity<PagedModel<ConfigurationDto>> {
        TODO("Not yet implemented")
    }

    override fun findAllNamespaces(
        name: String?,
        pageable: Pageable?,
    ): ResponseEntity<PagedModel<NamespaceDto>> {
        TODO("Not yet implemented")
    }

    override fun getApplication(
        nid: Long,
        aid: Long,
    ): ResponseEntity<ApplicationDto> {
        TODO("Not yet implemented")
    }

    override fun getApplicationAccessToken(
        nid: Long,
        aid: Long,
    ): ResponseEntity<AccessTokenDto> {
        TODO("Not yet implemented")
    }

    override fun getApplicationManagers(
        nid: Long,
        aid: Long,
    ): ResponseEntity<List<UserInfoDto>> {
        TODO("Not yet implemented")
    }

    override fun getConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
    ): ResponseEntity<ConfigurationDto> {
        TODO("Not yet implemented")
    }

    override fun getConfigurationSchema(
        nid: Long,
        aid: Long,
        cid: Long,
    ): ResponseEntity<String> {
        TODO("Not yet implemented")
    }

    override fun getConfigurationValues(
        nid: Long,
        aid: Long,
        cid: Long,
    ): ResponseEntity<String> {
        TODO("Not yet implemented")
    }

    override fun getConfigurationVersion(
        nid: Long,
        aid: Long,
        cid: Long,
        commitHash: String,
    ): ResponseEntity<ConfigurationVersionWithState> {
        TODO("Not yet implemented")
    }

    override fun getNamespace(nid: Long): ResponseEntity<NamespaceDto> {
        TODO("Not yet implemented")
    }

    override fun getNamespaceAdmins(nid: Long): ResponseEntity<List<UserInfoDto>> {
        TODO("Not yet implemented")
    }

    override fun removeApplicationManager(
        nid: Long,
        aid: Long,
        uid: UUID,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun removeNamespaceAdmin(
        nid: Long,
        uid: UUID,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun switchConfigurationVersion(
        nid: Long,
        aid: Long,
        cid: Long,
        commitHash: String,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }

    override fun updateApplication(
        nid: Long,
        aid: Long,
        applicationUpdateRequest: ApplicationUpdateRequest,
    ): ResponseEntity<ApplicationDto> {
        TODO("Not yet implemented")
    }

    override fun updateApplicationAccessToken(
        nid: Long,
        aid: Long,
        accessTokenDto: AccessTokenDto,
    ): ResponseEntity<AccessTokenDto> {
        TODO("Not yet implemented")
    }

    override fun updateConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
        configurationDtoUpdateRequest: ConfigurationDtoUpdateRequest,
    ): ResponseEntity<ConfigurationDto> {
        TODO("Not yet implemented")
    }

    override fun updateConfigurationSchema(
        nid: Long,
        aid: Long,
        cid: Long,
        body: String,
    ): ResponseEntity<ConfigurationVersion> {
        TODO("Not yet implemented")
    }

    override fun updateConfigurationValues(
        nid: Long,
        aid: Long,
        cid: Long,
        body: String,
    ): ResponseEntity<ConfigurationVersion> {
        TODO("Not yet implemented")
    }

    override fun updateNamespace(
        nid: Long,
        namespaceUpdateRequest: NamespaceUpdateRequest,
    ): ResponseEntity<NamespaceDto> {
        TODO("Not yet implemented")
    }

    override fun updateStreamStateConfiguration(
        nid: Long,
        aid: Long,
        cid: Long,
        configurationStreamState: ConfigurationStreamState,
    ): ResponseEntity<Unit> {
        TODO("Not yet implemented")
    }
}
