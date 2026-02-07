package ru.enzhine.rtcms4j.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import ru.enzhine.rtcms4j.core.api.event.NotificationEventDto
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonSchema
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonValues

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplateNotificationEventDto(
        objectMapper: ObjectMapper,
        redisConnectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, NotificationEventDto> =
        RedisTemplate<String, NotificationEventDto>()
            .apply {
                connectionFactory = redisConnectionFactory
                valueSerializer = Jackson2JsonRedisSerializer(objectMapper, NotificationEventDto::class.java)
            }

    @Bean
    fun redisTemplateCacheJsonSchema(
        objectMapper: ObjectMapper,
        redisConnectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, CacheJsonSchema> =
        RedisTemplate<String, CacheJsonSchema>()
            .apply {
                connectionFactory = redisConnectionFactory
                valueSerializer = Jackson2JsonRedisSerializer(objectMapper, CacheJsonSchema::class.java)
            }

    @Bean
    fun redisTemplateCacheJsonValues(
        objectMapper: ObjectMapper,
        redisConnectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, CacheJsonValues> =
        RedisTemplate<String, CacheJsonValues>()
            .apply {
                connectionFactory = redisConnectionFactory
                valueSerializer = Jackson2JsonRedisSerializer(objectMapper, CacheJsonValues::class.java)
            }
}
