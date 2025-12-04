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
import ru.enzhine.rtcms4j.core.builder.newConfigSchemaDetailedEntity
import ru.enzhine.rtcms4j.core.builder.newConfigurationEntity
import ru.enzhine.rtcms4j.core.builder.newNamespaceEntity
import ru.enzhine.rtcms4j.core.mapper.toUndetailed
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.ConfigSchemaEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.ConfigurationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
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
class ConfigSchemaEntityRepositoryImplTest {
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

    private val objectMapper = ObjectMapper()

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")
    private lateinit var namespace: NamespaceEntity
    private lateinit var application: ApplicationEntity
    private lateinit var configuration: ConfigurationEntity

    @BeforeEach
    fun clearTable() {
        jdbcTemplate.execute("truncate table namespace restart identity cascade;")
        jdbcTemplate.execute("truncate table application restart identity cascade;")
        jdbcTemplate.execute("truncate table configuration restart identity cascade;")
        jdbcTemplate.execute("truncate table config_schema restart identity cascade;")

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
    }

    private val jsonSchema =
        $$"""{"$schema": "http://json-schema.org/draft-07/schema#", "type": "object", "properties": { "_backwardCapability": {"type": "string"}, "_configurationLabel": {"type": "string"}}, "required": ["_backwardCapability", "_configurationLabel"], "additionalProperties": true}"""

    private val jsonSchema2 =
        $$"""{"$schema": "http://json-schema.org/draft-07/schema#", "type": "object", "properties": { "_backwardCapability": {"type": "string"}, "_configurationLabel": {"type": "string"}}, "required": ["_backwardCapability", "_configurationLabel"], "additionalProperties": false}"""

    @Test
    fun save_positive_success() {
        val templateEntity =
            newConfigSchemaDetailedEntity(
                configurationId = configuration.id,
                sourceType = SourceType.SERVICE,
                sourceIdentity = "App-1",
                jsonSchema = jsonSchema,
            )

        val created = configSchemaEntityRepository.save(templateEntity)
        Assertions.assertEquals(templateEntity.configurationId, created.configurationId)
        Assertions.assertEquals(templateEntity.sourceType, created.sourceType)
        Assertions.assertEquals(templateEntity.sourceIdentity, created.sourceIdentity)
        Assertions.assertEquals(
            objectMapper.readTree(templateEntity.jsonSchema),
            objectMapper.readTree(created.jsonSchema),
        )
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedJsonSchema_error() {
        val templateEntity =
            newConfigSchemaDetailedEntity(
                configurationId = configuration.id,
                sourceType = SourceType.SERVICE,
                sourceIdentity = "App-1",
                jsonSchema = jsonSchema,
            )

        configSchemaEntityRepository.save(templateEntity)
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            configSchemaEntityRepository.save(templateEntity)
        }
    }

    @Test
    fun save_configurationDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            configSchemaEntityRepository.save(
                newConfigSchemaDetailedEntity(
                    configurationId = 10,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    jsonSchema = jsonSchema,
                ),
            )
        }
    }

    @Test
    fun findAllByConfigurationId_positive_success() {
        val created =
            listOf(
                configSchemaEntityRepository
                    .save(
                        newConfigSchemaDetailedEntity(
                            configurationId = configuration.id,
                            sourceType = SourceType.SERVICE,
                            sourceIdentity = "App-1",
                            jsonSchema = jsonSchema,
                        ),
                    ).toUndetailed(),
                configSchemaEntityRepository
                    .save(
                        newConfigSchemaDetailedEntity(
                            configurationId = configuration.id,
                            sourceType = SourceType.SERVICE,
                            sourceIdentity = "App-1",
                            jsonSchema = jsonSchema2,
                        ),
                    ).toUndetailed(),
            )

        val list = configSchemaEntityRepository.findAllByConfigurationId(configuration.id)
        AssertionsJ.assertThat(list).containsAll(created)
    }

    @Test
    fun findById_positive_success() {
        val created =
            configSchemaEntityRepository.save(
                newConfigSchemaDetailedEntity(
                    configurationId = configuration.id,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    jsonSchema = jsonSchema,
                ),
            )

        val found = configSchemaEntityRepository.findById(created.id)
        Assertions.assertEquals(created, found)
    }

    @Test
    fun removeById_positive_success() {
        val created =
            configSchemaEntityRepository.save(
                newConfigSchemaDetailedEntity(
                    configurationId = configuration.id,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    jsonSchema = jsonSchema,
                ),
            )

        val result = configSchemaEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = configSchemaEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = configSchemaEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val list = configSchemaEntityRepository.findAllByConfigurationId(application.id)
        AssertionsJ.assertThat(list).isEmpty()
    }
}
