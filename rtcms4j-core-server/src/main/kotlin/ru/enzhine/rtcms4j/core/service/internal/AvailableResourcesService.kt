package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import java.util.UUID

interface AvailableResourcesService {
    fun findAvailableNamespaces(
        subject: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Namespace>

    fun findAvailableApplications(
        subject: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Application>
}
