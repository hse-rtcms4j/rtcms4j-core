package integration.ru.enzhine.rtcms4j.core.repository.kv

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import ru.enzhine.rtcms4j.core.config.RedisConfig
import ru.enzhine.rtcms4j.core.repository.kv.KeyValueRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonSchema
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheJsonValues
import ru.enzhine.rtcms4j.core.repository.kv.dto.CacheKey

@SpringBootTest(
    classes = [
        TestRedisConfiguration::class,
        RedisConfig::class,
        KeyValueRepositoryImpl::class,
    ],
)
@ImportAutoConfiguration(RedisAutoConfiguration::class)
@ActiveProfiles("test")
class KeyValueRepositoryImplTest {
    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate

    @Autowired
    private lateinit var keyValueRepository: KeyValueRepositoryImpl

    private val cacheKey =
        CacheKey(
            namespaceId = 1L,
            applicationId = 2L,
            configurationId = 3L,
        )

    @BeforeEach
    fun beforeEach() {
        redisTemplate.connectionFactory!!
            .connection
            .serverCommands()
            .flushDb()
    }

    @Test
    fun getCacheJsonValues_empty_success() {
        val cacheJsonValues = keyValueRepository.getCacheJsonValues(cacheKey)
        Assertions.assertNull(cacheJsonValues)
    }

    @Test
    fun getCacheJsonValues_put_success() {
        val cacheJsonValues = CacheJsonValues("{key:value}")
        keyValueRepository.putCacheJsonValues(cacheKey, cacheJsonValues)

        val cache = keyValueRepository.getCacheJsonValues(cacheKey)
        Assertions.assertEquals(cacheJsonValues, cache)
    }

    @Test
    fun getCacheJsonSchema_empty_success() {
        val cacheJsonSchema = keyValueRepository.getCacheJsonSchema(cacheKey)
        Assertions.assertNull(cacheJsonSchema)
    }

    @Test
    fun getCacheJsonSchema_put_success() {
        val cacheJsonSchema = CacheJsonSchema(10L, "{schema:schema}")
        keyValueRepository.putCacheJsonSchema(cacheKey, cacheJsonSchema)

        val cache = keyValueRepository.getCacheJsonSchema(cacheKey)
        Assertions.assertEquals(cacheJsonSchema, cache)
    }
}
