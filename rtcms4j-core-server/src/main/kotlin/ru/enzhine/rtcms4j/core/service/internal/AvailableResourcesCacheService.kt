package ru.enzhine.rtcms4j.core.service.internal

import ru.enzhine.rtcms4j.core.service.internal.dto.AvailableResources
import java.util.UUID

interface AvailableResourcesCacheService {
    fun getAvailableResourcesOrCache(subject: UUID): AvailableResources
}
