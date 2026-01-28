package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.core.config.CacheConfig
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepository
import ru.enzhine.rtcms4j.core.service.internal.dto.AvailableResources
import java.util.UUID

@Component
class AvailableResourcesCacheServiceImpl(
    private val namespaceEntityRepository: NamespaceEntityRepository,
    private val applicationEntityRepository: ApplicationEntityRepository,
) : AvailableResourcesCacheService {
    @Cacheable(
        cacheNames = [CacheConfig.AVAILABLE_RESOURCES_CACHE],
    )
    override fun getAvailableResourcesOrCache(subject: UUID): AvailableResources {
        val namespaces =
            namespaceEntityRepository
                .findAllByUserSub(subject)
                .map { it.toService() }
        val applications =
            applicationEntityRepository
                .findAllByUserSub(subject)
                .filter { namespaces.find { ns -> ns.id == it.namespaceId } == null }
                .map { it.toService() }

        return AvailableResources(
            namespaces = namespaces,
            applications = applications,
        )
    }
}
