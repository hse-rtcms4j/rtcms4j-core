package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.AvailableResources
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import java.util.UUID

interface AvailableResourcesService {
    fun getAvailableResources(userSub: UUID): AvailableResources

    fun findAvailableNamespaces(
        userSub: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Namespace>

    fun findAvailableApplications(
        userSub: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Application>
}
