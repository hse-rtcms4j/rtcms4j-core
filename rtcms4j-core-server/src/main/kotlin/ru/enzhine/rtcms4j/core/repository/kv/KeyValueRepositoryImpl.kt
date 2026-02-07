package ru.enzhine.rtcms4j.core.repository.kv

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.config.props.KeyValRepositoryProperties
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonSchema
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonValues
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheKey

@Repository
class KeyValueRepositoryImpl(
    private val cacheJsonSchemaTemplate: RedisTemplate<String, CacheJsonSchema>,
    private val cacheJsonValuesTemplate: RedisTemplate<String, CacheJsonValues>,
    private val keyValRepositoryProperties: KeyValRepositoryProperties,
) : KeyValueRepository {
    override fun putCacheJsonSchema(
        cacheKey: CacheKey,
        cacheJsonSchema: CacheJsonSchema,
    ) {
        val key = buildConfigSchemaKey(keyValRepositoryProperties, cacheKey)
        cacheJsonSchemaTemplate.opsForValue().set(key, cacheJsonSchema)
    }

    override fun getCacheJsonSchema(cacheKey: CacheKey): CacheJsonSchema? {
        val key = buildConfigSchemaKey(keyValRepositoryProperties, cacheKey)
        return cacheJsonSchemaTemplate.opsForValue().get(key)
    }

    override fun putCacheJsonValues(
        cacheKey: CacheKey,
        cacheJsonValues: CacheJsonValues,
    ) {
        val key = buildConfigValuesKey(keyValRepositoryProperties, cacheKey)
        cacheJsonValuesTemplate.opsForValue().set(key, cacheJsonValues)
    }

    override fun getCacheJsonValues(cacheKey: CacheKey): CacheJsonValues? {
        val key = buildConfigValuesKey(keyValRepositoryProperties, cacheKey)
        return cacheJsonValuesTemplate.opsForValue().get(key)
    }

    private fun buildKeyPrefix(
        properties: KeyValRepositoryProperties,
        cacheKey: CacheKey,
    ) = "${properties.globalPrefix}${cacheKey.namespaceId}_${cacheKey.applicationId}_${cacheKey.configurationId}"

    private fun buildConfigSchemaKey(
        properties: KeyValRepositoryProperties,
        cacheKey: CacheKey,
    ) = buildKeyPrefix(properties, cacheKey) + "_SCHEMA"

    private fun buildConfigValuesKey(
        properties: KeyValRepositoryProperties,
        cacheKey: CacheKey,
    ) = buildKeyPrefix(properties, cacheKey) + "_VALUES"
}
