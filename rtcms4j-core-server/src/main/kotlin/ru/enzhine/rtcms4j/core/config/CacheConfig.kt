package ru.enzhine.rtcms4j.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import ru.enzhine.rtcms4j.core.config.props.CacheProperties
import ru.enzhine.rtcms4j.core.config.props.KeyValRepositoryProperties
import ru.enzhine.rtcms4j.core.service.external.dto.KeycloakUser
import ru.enzhine.rtcms4j.core.service.internal.dto.AvailableResources

@Configuration
@EnableCaching
class CacheConfig {
    companion object {
        const val KEYCLOAK_USERS_CACHE = "keycloak-users"
        const val AVAILABLE_RESOURCES_CACHE = "available-resources"
    }

    @Bean
    fun redisCacheManagerBuilderCustomizer(
        objectMapper: ObjectMapper,
        kvProperties: KeyValRepositoryProperties,
        cacheProperties: CacheProperties,
    ): RedisCacheManagerBuilderCustomizer =
        RedisCacheManagerBuilderCustomizer {
            it.withCacheConfiguration(
                KEYCLOAK_USERS_CACHE,
                RedisCacheConfiguration
                    .defaultCacheConfig()
                    .entryTtl(cacheProperties.keycloakUsersTtl)
                    .disableCachingNullValues()
                    .prefixCacheNameWith(buildKey(kvProperties, KEYCLOAK_USERS_CACHE))
                    .serializeValuesWith(
                        keySerializationPair(
                            Jackson2JsonRedisSerializer(
                                objectMapper,
                                KeycloakUser::class.java,
                            ),
                        ),
                    ),
            )

            it.withCacheConfiguration(
                AVAILABLE_RESOURCES_CACHE,
                RedisCacheConfiguration
                    .defaultCacheConfig()
                    .entryTtl(cacheProperties.availableResourcesTtl)
                    .disableCachingNullValues()
                    .prefixCacheNameWith(buildKey(kvProperties, AVAILABLE_RESOURCES_CACHE))
                    .serializeValuesWith(
                        keySerializationPair(
                            Jackson2JsonRedisSerializer(
                                objectMapper,
                                AvailableResources::class.java,
                            ),
                        ),
                    ),
            )
        }

    private fun buildKey(
        properties: KeyValRepositoryProperties,
        key: String,
    ) = properties.globalPrefix + key

    private fun <T> keySerializationPair(serializer: org.springframework.data.redis.serializer.RedisSerializer<T>) =
        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
}
