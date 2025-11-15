package integration.ru.enzhine.rtcms4j.core.repository

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
import ru.enzhine.rtcms4j.core.repository.ApplicationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.ConfigurationCommitEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.ConfigurationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import ru.enzhine.rtcms4j.core.builder.newApplicationEntity
import ru.enzhine.rtcms4j.core.builder.newConfigurationCommitEntity
import ru.enzhine.rtcms4j.core.builder.newConfigurationEntity
import ru.enzhine.rtcms4j.core.builder.newNamespaceEntity
import java.util.UUID
import kotlin.jvm.java
import org.assertj.core.api.Assertions as AssertionsJ

@SpringBootTest(
    classes = [
        NamespaceEntityRepositoryImpl::class,
        ApplicationEntityRepositoryImpl::class,
        ConfigurationEntityRepositoryImpl::class,
        ConfigurationCommitEntityRepositoryImpl::class,
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
class ConfigurationCommitEntityRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    @Autowired
    private lateinit var applicationEntityRepository: ApplicationEntityRepositoryImpl

    @Autowired
    private lateinit var configurationEntityRepository: ConfigurationEntityRepositoryImpl

    @Autowired
    private lateinit var configurationCommitEntityRepository: ConfigurationCommitEntityRepositoryImpl

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
        jdbcTemplate.execute("truncate table configuration_commit restart identity cascade;")

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
                    accessToken = "kashdvn817t17envoaidjjvna75as65aios9y",
                ),
            )

        configuration =
            configurationEntityRepository.save(
                newConfigurationEntity(
                    applicationId = application.id,
                    creatorSub = sub,
                    name = "MainDto",
                    usedCommitHash = null,
                    streamKey = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )
    }

    private val jsonValues = "{\"_backwardCapability\": \"false\", \"_configurationLabel\": \"MainDto\"}"
    private val jsonSchema =
        $$"{\"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": { \"_backwardCapability\": {\"type\": \"string\"}, \"_configurationLabel\": {\"type\": \"string\"}}, \"required\": [\"_backwardCapability\", \"_configurationLabel\"], \"additionalProperties\": true}"

    @Test
    fun save_positive_success() {
        val templateEntity =
            newConfigurationCommitEntity(
                configuration.id,
                sourceType = SourceType.SERVICE,
                sourceIdentity = "App-1",
                commitHash = "a1b1",
                jsonValues = jsonValues,
                jsonSchema = jsonSchema,
            )

        val created = configurationCommitEntityRepository.save(templateEntity)
        Assertions.assertEquals(templateEntity.configurationId, created.configurationId)
        Assertions.assertEquals(templateEntity.sourceType, created.sourceType)
        Assertions.assertEquals(templateEntity.sourceIdentity, created.sourceIdentity)
        Assertions.assertEquals(templateEntity.commitHash, created.commitHash)
        Assertions.assertEquals(objectMapper.readTree(templateEntity.jsonValues), objectMapper.readTree(created.jsonValues))
        Assertions.assertEquals(objectMapper.readTree(templateEntity.jsonSchema), objectMapper.readTree(created.jsonSchema))
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedCommitHash_error() {
        val templateEntity =
            newConfigurationCommitEntity(
                configuration.id,
                sourceType = SourceType.SERVICE,
                sourceIdentity = "App-1",
                commitHash = "a1b1",
                jsonValues = jsonValues,
                jsonSchema = jsonSchema,
            )

        configurationCommitEntityRepository.save(templateEntity)
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            configurationCommitEntityRepository.save(templateEntity)
        }
    }

    @Test
    fun save_configurationDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            configurationCommitEntityRepository.save(
                newConfigurationCommitEntity(
                    10,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    jsonValues = jsonValues,
                    jsonSchema = jsonSchema,
                ),
            )
        }
    }

    @Test
    fun findAllByConfigurationId_positive_success() {
        val created =
            listOf(
                configurationCommitEntityRepository.save(
                    newConfigurationCommitEntity(
                        configuration.id,
                        sourceType = SourceType.SERVICE,
                        sourceIdentity = "App-1",
                        commitHash = "a1b1",
                        jsonValues = jsonValues,
                        jsonSchema = jsonSchema,
                    ),
                ),
                configurationCommitEntityRepository.save(
                    newConfigurationCommitEntity(
                        configuration.id,
                        sourceType = SourceType.SERVICE,
                        sourceIdentity = "App-1",
                        commitHash = "a2b2",
                        jsonValues = jsonValues,
                        jsonSchema = jsonSchema,
                    ),
                ),
            )

        val list = configurationCommitEntityRepository.findAllByConfigurationId(configuration.id)
        AssertionsJ.assertThat(list).containsAll(created)
    }

    @Test
    fun findByConfigurationIdAndCommitHash_positive_success() {
        val created1 =
            configurationCommitEntityRepository.save(
                newConfigurationCommitEntity(
                    configuration.id,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    jsonValues = jsonValues,
                    jsonSchema = jsonSchema,
                ),
            )
        configurationCommitEntityRepository.save(
            newConfigurationCommitEntity(
                configuration.id,
                sourceType = SourceType.SERVICE,
                sourceIdentity = "App-1",
                commitHash = "a2b2",
                jsonValues = jsonValues,
                jsonSchema = jsonSchema,
            ),
        )

        val found =
            configurationCommitEntityRepository.findByConfigurationIdAndCommitHash(
                created1.id,
                created1.commitHash,
            )
        Assertions.assertEquals(created1, found)
    }

    @Test
    fun findById_positive_success() {
        val created =
            configurationCommitEntityRepository.save(
                newConfigurationCommitEntity(
                    configuration.id,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    jsonValues = jsonValues,
                    jsonSchema = jsonSchema,
                ),
            )

        val found = configurationCommitEntityRepository.findById(created.id)
        Assertions.assertEquals(created, found)
    }

    @Test
    fun removeById_positive_success() {
        val created =
            configurationCommitEntityRepository.save(
                newConfigurationCommitEntity(
                    configuration.id,
                    sourceType = SourceType.SERVICE,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    jsonValues = jsonValues,
                    jsonSchema = jsonSchema,
                ),
            )

        val result = configurationCommitEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = configurationCommitEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = configurationCommitEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val list = configurationCommitEntityRepository.findAllByConfigurationId(application.id)
        AssertionsJ.assertThat(list).isEmpty()
    }
}
