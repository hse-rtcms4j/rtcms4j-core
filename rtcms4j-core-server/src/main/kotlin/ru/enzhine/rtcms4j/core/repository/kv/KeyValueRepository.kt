package ru.enzhine.rtcms4j.core.repository.kv

import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonSchema
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonValues
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheKey

interface KeyValueRepository {
    fun putCacheJsonSchema(
        cacheKey: CacheKey,
        cacheJsonSchema: CacheJsonSchema,
    )

    fun getCacheJsonSchema(cacheKey: CacheKey): CacheJsonSchema?

    fun putCacheJsonValues(
        cacheKey: CacheKey,
        cacheJsonValues: CacheJsonValues,
    )

    fun getCacheJsonValues(cacheKey: CacheKey): CacheJsonValues?
}
