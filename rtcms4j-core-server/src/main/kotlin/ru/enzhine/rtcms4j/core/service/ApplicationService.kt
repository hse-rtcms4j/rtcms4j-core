package ru.enzhine.rtcms4j.core.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.service.dto.Application
import java.util.UUID

interface ApplicationService {
    fun createApplication(creator: UUID, namespaceId: Long, name: String, description: String): Application

    fun getApplicationById(namespaceId: Long, id: Long): Application

    fun updateApplication(application: Application): Application

    fun findApplications(namespaceId: Long, name: String?, pageable: Pageable?): Page<Application>

    fun deleteApplication(application: Application): Boolean

    fun listManagers(application: Application): List<UUID>
    
    fun addManager(application: Application, sub: UUID): Boolean

    fun removeManager(application: Application, sub: UUID): Boolean
}
