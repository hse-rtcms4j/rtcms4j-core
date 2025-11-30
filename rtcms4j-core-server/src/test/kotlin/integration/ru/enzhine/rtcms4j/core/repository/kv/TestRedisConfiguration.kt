package integration.ru.enzhine.rtcms4j.core.repository.kv

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import redis.embedded.RedisServer
import ru.enzhine.rtcms4j.core.config.props.KeyValRepositoryProperties

@TestConfiguration
class TestRedisConfiguration(
    redisProperties: RedisProperties,
) {
    private val redisServer = RedisServer(redisProperties.port)

    @Bean
    fun keyValRepositoryProperties() =
        KeyValRepositoryProperties(
            globalPrefix = "RTCMS4J_CORE_SCOPE_",
            topic = "NOTIFY_EVENT_TOPIC",
        )

    @Bean
    fun redisServer() = redisServer

    @PostConstruct
    fun postConstruct() {
        redisServer.start()
    }

    @PreDestroy
    fun preDestroy() {
        redisServer.stop()
    }
}
