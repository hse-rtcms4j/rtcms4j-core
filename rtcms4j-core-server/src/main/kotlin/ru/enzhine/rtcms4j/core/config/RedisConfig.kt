package ru.enzhine.rtcms4j.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import ru.enzhine.rtcms4j.core.repository.kv.dto.NotifyEventDto

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
        notifyEventDtoRedisSerializer: RedisSerializer<NotifyEventDto>,
    ): RedisTemplate<String, NotifyEventDto> {
        val redisTemplate = RedisTemplate<String, NotifyEventDto>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.valueSerializer = notifyEventDtoRedisSerializer
        return redisTemplate
    }
}
