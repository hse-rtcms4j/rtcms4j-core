package ru.enzhine.rtcms4j.core.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.service.dto.Application
import java.util.UUID

interface ApplicationService {
    @Throws(ConditionFailureException.KeyDuplicated::class)
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
    ): Application

    @Throws(ConditionFailureException.NotFound::class, ConditionFailureException.KeyDuplicated::class)
    fun updateApplication(
        namespaceId: Long,
        applicationId: Long,
        name: String?,
        description: String?,
        accessToken: String?,
    ): Application

    fun findApplications(
        namespaceId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Application>

    fun deleteApplication(
        namespaceId: Long,
        applicationId: Long,
    ): Boolean

    fun listManagers(
        namespaceId: Long,
        applicationId: Long,
    ): List<UUID>

    fun addManager(
        assigner: UUID,
        namespaceId: Long,
        applicationId: Long,
        sub: UUID,
    ): Boolean

    fun removeManager(
        namespaceId: Long,
        applicationId: Long,
        sub: UUID,
    ): Boolean
}
