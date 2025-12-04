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
import ru.enzhine.rtcms4j.core.repository.kv.KeyValueRepositoryImpl
import ru.enzhine.rtcms4j.core.service.internal.dto.Configuration
import ru.enzhine.rtcms4j.core.service.internal.dto.SourceType

@SpringBootTest(
    classes = [
        TestRedisConfiguration::class,
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

    private val configuration =
        Configuration(
            id = 1L,
            namespaceId = 2L,
            applicationId = 3L,
            name = "DefaultDto",
            schemaSourceType = SourceType.SERVICE,
            actualCommitId = null,
        )

    @BeforeEach
    fun beforeEach() {
        redisTemplate.connectionFactory!!
            .connection
            .serverCommands()
            .flushDb()
    }

    @Test
    fun getConfigurationValues_empty_success() {
        val cache = keyValueRepository.getConfigurationValues(configuration)
        Assertions.assertNull(cache)
    }

    @Test
    fun getConfigurationValues_put_success() {
        val schema = "{key:value}"
        keyValueRepository.putConfigurationValues(configuration, schema)

        val cache = keyValueRepository.getConfigurationValues(configuration)
        Assertions.assertEquals(schema, cache)
    }

    @Test
    fun getConfigurationSchema_empty_success() {
        val cache = keyValueRepository.getConfigurationSchema(configuration)
        Assertions.assertNull(cache)
    }

    @Test
    fun getConfigurationSchema_put_success() {
        val schema = "{schema:schema}"
        keyValueRepository.putConfigurationSchema(configuration, schema)

        val cache = keyValueRepository.getConfigurationSchema(configuration)
        Assertions.assertEquals(schema, cache)
    }
}
