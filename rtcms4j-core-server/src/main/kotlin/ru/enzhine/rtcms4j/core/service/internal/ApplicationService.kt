package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.KeycloakClient
import java.util.UUID

interface ApplicationService {
    @Throws(ConditionFailureException.NotFound::class, ConditionFailureException.KeyDuplicated::class)
    fun createApplication(
        creator: UUID,
        namespaceId: Long,
        name: String,
        description: String,
    ): Application

    @Throws(ConditionFailureException.NotFound::class)
    fun getApplicationById(
        namespaceId: Long,
        applicationId: Long,
        forShare: Boolean,
    ): Application

    @Throws(ConditionFailureException.NotFound::class, ConditionFailureException.KeyDuplicated::class)
    fun updateApplication(
        namespaceId: Long,
        applicationId: Long,
        name: String?,
        description: String?,
    ): Application

    @Throws(ConditionFailureException.NotFound::class)
    fun getApplicationClientCredentials(
        namespaceId: Long,
        applicationId: Long,
    ): KeycloakClient

    @Throws(ConditionFailureException.NotFound::class)
    fun rotateApplicationClientCredentials(
        namespaceId: Long,
        applicationId: Long,
    ): KeycloakClient

    @Throws(ConditionFailureException.NotFound::class)
    fun findApplications(
        namespaceId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Application>

    @Throws(ConditionFailureException.NotFound::class)
    fun deleteApplication(
        namespaceId: Long,
        applicationId: Long,
    ): Boolean

    @Throws(ConditionFailureException.NotFound::class)
    fun listManagers(
        namespaceId: Long,
        applicationId: Long,
    ): List<UUID>

    @Throws(ConditionFailureException.NotFound::class)
    fun addManager(
        assigner: UUID,
        namespaceId: Long,
        applicationId: Long,
        sub: UUID,
    ): Boolean

    @Throws(ConditionFailureException.NotFound::class)
    fun removeManager(
        namespaceId: Long,
        applicationId: Long,
        sub: UUID,
    ): Boolean
}
