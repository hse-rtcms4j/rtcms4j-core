package ru.enzhine.rtcms4j.core.repository.kv

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.config.props.KeyValRepositoryProperties
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration

@Repository
class KeyValueRepositoryImpl(
    private val stringTemplate: StringRedisTemplate,
    private val keyValRepositoryProperties: KeyValRepositoryProperties,
) : KeyValueRepository {
    override fun putConfigurationSchema(
        configuration: Configuration,
        schema: String,
    ) {
        val key = buildConfigSchemaKey(keyValRepositoryProperties, configuration)
        stringTemplate.opsForValue().set(key, schema)
    }

    override fun getConfigurationSchema(configuration: Configuration): String? {
        val key = buildConfigSchemaKey(keyValRepositoryProperties, configuration)
        return stringTemplate.opsForValue().get(key)
    }

    override fun putConfigurationValues(
        configuration: Configuration,
        values: String,
    ) {
        val key = buildConfigValuesKey(keyValRepositoryProperties, configuration)
        stringTemplate.opsForValue().set(key, values)
    }

    override fun getConfigurationValues(configuration: Configuration): String? {
        val key = buildConfigValuesKey(keyValRepositoryProperties, configuration)
        return stringTemplate.opsForValue().get(key)
    }

    private fun buildConfigKey(
        properties: KeyValRepositoryProperties,
        configuration: Configuration,
    ) = properties.globalPrefix + "${configuration.namespaceId}_${configuration.applicationId}_${configuration.id}"

    private fun buildConfigSchemaKey(
        properties: KeyValRepositoryProperties,
        configuration: Configuration,
    ) = buildConfigKey(properties, configuration) + "_SCHEMA"

    private fun buildConfigValuesKey(
        properties: KeyValRepositoryProperties,
        configuration: Configuration,
    ) = buildConfigKey(properties, configuration) + "_VALUES"
}
