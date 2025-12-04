package integration.ru.enzhine.rtcms4j.core.repository.db

import com.fasterxml.jackson.databind.ObjectMapper
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.enzhine.rtcms4j.core.builder.newApplicationEntity
import ru.enzhine.rtcms4j.core.builder.newConfigCommitDetailedEntity
import ru.enzhine.rtcms4j.core.builder.newConfigSchemaDetailedEntity
import ru.enzhine.rtcms4j.core.builder.newConfigurationEntity
import ru.enzhine.rtcms4j.core.builder.newNamespaceEntity
import ru.enzhine.rtcms4j.core.mapper.toUndetailed
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.ConfigCommitEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.ConfigSchemaEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.ConfigurationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType
import java.util.UUID
import kotlin.jvm.java
import org.assertj.core.api.Assertions as AssertionsJ

@SpringBootTest(
    classes = [
        NamespaceEntityRepositoryImpl::class,
        ApplicationEntityRepositoryImpl::class,
        ConfigurationEntityRepositoryImpl::class,
        ConfigSchemaEntityRepositoryImpl::class,
        ConfigCommitEntityRepositoryImpl::class,
    ],
)
@ImportAutoConfiguration(
    DataSourceAutoConfiguration::class,
    DataSourceTransactionManagerAutoConfiguration::class,
    JdbcTemplateAutoConfiguration::class,
    LiquibaseAutoConfiguration::class,
)
@AutoConfigureEmbeddedDatabase(
    provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY,
    type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES,
)
@ActiveProfiles("test")
class ConfigCommitEntityRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    @Autowired
    private lateinit var applicationEntityRepository: ApplicationEntityRepositoryImpl

    @Autowired
    private lateinit var configurationEntityRepository: ConfigurationEntityRepositoryImpl

    @Autowired
    private lateinit var configSchemaEntityRepository: ConfigSchemaEntityRepositoryImpl

    @Autowired
    private lateinit var configCommitEntityRepository: ConfigCommitEntityRepositoryImpl

    private val objectMapper = ObjectMapper()

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")

    private val jsonSchema =
        $$"""{"$schema": "http://json-schema.org/draft-07/schema#", "type": "object", "properties": { "_backwardCapability": {"type": "string"}, "_configurationLabel": {"type": "string"}}, "required": ["_backwardCapability", "_configurationLabel"], "additionalProperties": true}"""

    private lateinit var namespace: NamespaceEntity
    private lateinit var application: ApplicationEntity
    private lateinit var configuration: ConfigurationEntity
    private lateinit var configSchema: ConfigSchemaDetailedEntity

    @BeforeEach
    fun clearTable() {
        jdbcTemplate.execute("truncate table namespace restart identity cascade;")
        jdbcTemplate.execute("truncate table application restart identity cascade;")
        jdbcTemplate.execute("truncate table configuration restart identity cascade;")
        jdbcTemplate.execute("truncate table config_schema restart identity cascade;")
        jdbcTemplate.execute("truncate table config_commit restart identity cascade;")

        namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports serving team",
                ),
            )

        application =
            applicationEntityRepository.save(
                newApplicationEntity(
                    namespaceId = namespace.id,
                    creatorSub = sub,
                    name = "Registry",
                    description = "Clients data storage service",
                ),
            )

        configuration =
            configurationEntityRepository.save(
                newConfigurationEntity(
                    applicationId = application.id,
                    creatorSub = sub,
                    name = "MainDto",
                    schemaSourceType = SourceType.SERVICE,
                    actualCommitId = null,
                ),
            )

        configSchema =
            configSchemaEntityRepository.save(
                newConfigSchemaDetailedEntity(
                    configuration.id,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    jsonSchema = jsonSchema,
                ),
            )
    }

    private val jsonValues =
        $$"""{"key": "values"}"""

    private val jsonValues2 =
        $$"""{"key1": "values2"}"""

    @Test
    fun save_positive_success() {
        val templateEntity =
            newConfigCommitDetailedEntity(
                configSchemaId = configSchema.id,
                configurationId = configSchema.configurationId,
                sourceType = SourceType.SERVICE,
                sourceIdentity = "App-1",
                jsonValues = jsonValues,
            )

        val created = configCommitEntityRepository.save(templateEntity)
        Assertions.assertEquals(templateEntity.configSchemaId, created.configSchemaId)
        Assertions.assertEquals(templateEntity.sourceType, created.sourceType)
        Assertions.assertEquals(templateEntity.sourceIdentity, created.sourceIdentity)
        Assertions.assertEquals(objectMapper.readTree(templateEntity.jsonValues), objectMapper.readTree(created.jsonValues))
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedJsonSchema_error() {
        val templateEntity =
            newConfigCommitDetailedEntity(
                configSchemaId = configSchema.id,
                configurationId = configSchema.configurationId,
                sourceType = SourceType.SERVICE,
                sourceIdentity = "App-1",
                jsonValues = jsonValues,
            )

        configCommitEntityRepository.save(templateEntity)
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            configCommitEntityRepository.save(templateEntity)
        }
    }

    @Test
    fun save_configurationDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            configCommitEntityRepository.save(
                newConfigCommitDetailedEntity(
                    10,
                    configurationId = configSchema.configurationId,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    jsonValues = jsonValues,
                ),
            )
        }
    }

    @Test
    fun findAllByConfigurationId_positive_success() {
        val created =
            listOf(
                configCommitEntityRepository
                    .save(
                        newConfigCommitDetailedEntity(
                            configSchemaId = configSchema.id,
                            configurationId = configSchema.configurationId,
                            sourceType = SourceType.SERVICE,
                            sourceIdentity = "App-1",
                            jsonValues = jsonValues,
                        ),
                    ).toUndetailed(),
                configCommitEntityRepository
                    .save(
                        newConfigCommitDetailedEntity(
                            configSchemaId = configSchema.id,
                            configurationId = configSchema.configurationId,
                            sourceType = SourceType.SERVICE,
                            sourceIdentity = "App-1",
                            jsonValues = jsonValues2,
                        ),
                    ).toUndetailed(),
            )

        val list = configCommitEntityRepository.findAllByConfigSchemaId(configSchema.id)
        AssertionsJ.assertThat(list).containsAll(created)
    }

    @Test
    fun findById_positive_success() {
        val created =
            configCommitEntityRepository.save(
                newConfigCommitDetailedEntity(
                    configSchemaId = configSchema.id,
                    configurationId = configSchema.configurationId,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    jsonValues = jsonValues,
                ),
            )

        val found = configCommitEntityRepository.findById(created.id)
        Assertions.assertEquals(created, found)
    }

    @Test
    fun removeById_positive_success() {
        val created =
            configCommitEntityRepository.save(
                newConfigCommitDetailedEntity(
                    configSchemaId = configSchema.id,
                    configurationId = configSchema.configurationId,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    jsonValues = jsonValues,
                ),
            )

        val result = configCommitEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = configCommitEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = configCommitEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val list = configCommitEntityRepository.findAllByConfigSchemaId(configSchema.id)
        AssertionsJ.assertThat(list).isEmpty()
    }
}
