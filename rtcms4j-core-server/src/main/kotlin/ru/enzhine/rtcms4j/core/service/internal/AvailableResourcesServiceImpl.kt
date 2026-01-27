package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import java.util.UUID
import kotlin.math.min

@Service
class AvailableResourcesServiceImpl(
    private val availableResourcesCacheService: AvailableResourcesCacheService,
    private val defaultPaginationProperties: DefaultPaginationProperties,
) : AvailableResourcesService {
    override fun findAvailableNamespaces(
        subject: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Namespace> {
        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return availableResourcesCacheService
            .getAvailableResourcesOrCache(subject)
            .namespaces
            .pagedBy(pageable) { name == null || it.name.contains(name) }
    }

    override fun findAvailableApplications(
        subject: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Application> {
        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return availableResourcesCacheService
            .getAvailableResourcesOrCache(subject)
            .applications
            .pagedBy(pageable) { name == null || it.name.contains(name) }
    }

    private fun <T> List<T>.pagedBy(
        pageable: Pageable,
        predicate: (T) -> Boolean,
    ): Page<T> {
        val total = this.size.toLong()
        val offset = pageable.offset.toInt()
        val filtered = this.filter { predicate(it) }
        val content = filtered.subList(offset, min(filtered.size, offset + pageable.pageSize))

        return PageImpl(content, pageable, total)
    }
}
