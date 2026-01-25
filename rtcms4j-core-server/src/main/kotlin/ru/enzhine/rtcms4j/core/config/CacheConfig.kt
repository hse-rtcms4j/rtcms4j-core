package ru.enzhine.rtcms4j.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
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
    fun redisManager(
        objectMapper: ObjectMapper,
        kvProperties: KeyValRepositoryProperties,
        cacheProperties: CacheProperties,
        redisConnectionFactory: RedisConnectionFactory,
    ): CacheManager {
        val keycloakUsersCacheConfig =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(cacheProperties.keycloakUsersTtl)
                .disableCachingNullValues()
                .prefixCacheNameWith(buildKey(kvProperties, KEYCLOAK_USERS_CACHE))
                .serializeKeysWith(keySerializationPair(StringRedisSerializer()))
                .serializeValuesWith(
                    keySerializationPair(
                        Jackson2JsonRedisSerializer(
                            objectMapper,
                            KeycloakUser::class.java,
                        ),
                    ),
                )

        val availableResourcesCacheConfig =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(cacheProperties.availableResourcesTtl)
                .disableCachingNullValues()
                .prefixCacheNameWith(buildKey(kvProperties, AVAILABLE_RESOURCES_CACHE))
                .serializeKeysWith(keySerializationPair(StringRedisSerializer()))
                .serializeValuesWith(
                    keySerializationPair(
                        Jackson2JsonRedisSerializer(
                            objectMapper,
                            AvailableResources::class.java,
                        ),
                    ),
                )

        val cacheConfiguration =
            mapOf(
                KEYCLOAK_USERS_CACHE to keycloakUsersCacheConfig,
                AVAILABLE_RESOURCES_CACHE to availableResourcesCacheConfig,
            )

        return RedisCacheManager
            .builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfiguration)
            .build()
    }

    private fun buildKey(
        properties: KeyValRepositoryProperties,
        key: String,
    ) = properties.globalPrefix + key

    private fun <T> keySerializationPair(serializer: org.springframework.data.redis.serializer.RedisSerializer<T>) =
        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
}
