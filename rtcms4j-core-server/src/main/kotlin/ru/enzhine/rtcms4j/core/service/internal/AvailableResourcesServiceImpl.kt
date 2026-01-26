package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.core.config.CacheConfig
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepository
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.AvailableResources
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import java.util.UUID
import kotlin.math.min

@Service
class AvailableResourcesServiceImpl(
    private val namespaceEntityRepository: NamespaceEntityRepository,
    private val applicationEntityRepository: ApplicationEntityRepository,
    private val defaultPaginationProperties: DefaultPaginationProperties,
) : AvailableResourcesService {
    @Cacheable(
        cacheNames = [CacheConfig.AVAILABLE_RESOURCES_CACHE],
    )
    override fun getAvailableResources(userSub: UUID): AvailableResources {
        val namespaces =
            namespaceEntityRepository
                .findAllByUserSub(userSub)
                .map { it.toService() }
        val applications =
            applicationEntityRepository
                .findAllByUserSub(userSub)
                .filter { namespaces.find { ns -> ns.id == it.namespaceId } == null }
                .map { it.toService() }

        return AvailableResources(
            namespaces = namespaces,
            applications = applications,
        )
    }

    override fun findAvailableNamespaces(
        userSub: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Namespace> {
        val availableResources = getAvailableResources(userSub)

        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return availableResources.namespaces
            .pagedBy(pageable) { name == null || it.name.contains(name) }
    }

    override fun findAvailableApplications(
        userSub: UUID,
        name: String?,
        pageable: Pageable?,
    ): Page<Application> {
        val availableResources = getAvailableResources(userSub)

        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return availableResources.applications
            .pagedBy(pageable) { name == null || it.name.contains(name) }
    }

    private fun <T> List<T>.pagedBy(
        pageable: Pageable,
        predicate: (T) -> Boolean,
    ): Page<T> {
        val total = this.size.toLong()
        val offset = pageable.offset.toInt()
        val content =
            this
                .filter { predicate(it) }
                .subList(offset, min(this.size, offset + pageable.pageSize))

        return PageImpl(content, pageable, total)
    }
}
