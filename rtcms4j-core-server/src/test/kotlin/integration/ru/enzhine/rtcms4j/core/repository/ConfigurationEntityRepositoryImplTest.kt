package integration.ru.enzhine.rtcms4j.core.repository

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
import ru.enzhine.rtcms4j.core.repository.ConfigurationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import ru.enzhine.rtcms4j.core.repository.dto.newApplicationEntity
import ru.enzhine.rtcms4j.core.repository.dto.newConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.dto.newNamespaceEntity
import java.util.UUID
import kotlin.jvm.java
import org.assertj.core.api.Assertions as AssertionsJ

@SpringBootTest(
    classes = [
        NamespaceEntityRepositoryImpl::class,
        ApplicationEntityRepositoryImpl::class,
        ConfigurationEntityRepositoryImpl::class,
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
class ConfigurationEntityRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    @Autowired
    private lateinit var applicationEntityRepository: ApplicationEntityRepositoryImpl

    @Autowired
    private lateinit var configurationEntityRepository: ConfigurationEntityRepositoryImpl

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")

    private lateinit var namespace: NamespaceEntity

    private lateinit var application: ApplicationEntity

    @BeforeEach
    fun clearTable() {
        jdbcTemplate.execute("truncate table namespace restart identity cascade;")
        jdbcTemplate.execute("truncate table application restart identity cascade;")
        jdbcTemplate.execute("truncate table configuration restart identity cascade;")

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
    }

    @Test
    fun save_positive_success() {
        val templateEntity =
            newConfigurationEntity(
                application.id,
                creatorSub = sub,
                name = "MainDto",
                usedCommitHash = null,
                streamKey = null,
                schemaSourceType = SourceType.SERVICE,
            )

        val created = configurationEntityRepository.save(templateEntity)
        Assertions.assertEquals(templateEntity.creatorSub, created.creatorSub)
        Assertions.assertEquals(templateEntity.applicationId, created.applicationId)
        Assertions.assertEquals(templateEntity.name, created.name)
        Assertions.assertEquals(templateEntity.usedCommitHash, created.usedCommitHash)
        Assertions.assertEquals(templateEntity.streamKey, created.streamKey)
        Assertions.assertEquals(templateEntity.schemaSourceType, created.schemaSourceType)
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedName_error() {
        val templateEntity =
            newConfigurationEntity(
                application.id,
                creatorSub = sub,
                name = "MainDto",
                usedCommitHash = null,
                streamKey = null,
                schemaSourceType = SourceType.SERVICE,
            )

        configurationEntityRepository.save(templateEntity)
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            configurationEntityRepository.save(templateEntity)
        }
    }

    @Test
    fun save_applicationDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            configurationEntityRepository.save(
                newConfigurationEntity(
                    10,
                    creatorSub = sub,
                    name = "MainDto",
                    usedCommitHash = null,
                    streamKey = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )
        }
    }

    @Test
    fun findAllByApplicationId_positive_success() {
        val created =
            configurationEntityRepository.save(
                newConfigurationEntity(
                    application.id,
                    creatorSub = sub,
                    name = "MainDto",
                    usedCommitHash = null,
                    streamKey = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )

        val list = configurationEntityRepository.findAllByApplicationId(application.id)
        AssertionsJ.assertThat(list).contains(created)
    }

    @Test
    fun findById_positive_success() {
        val created =
            configurationEntityRepository.save(
                newConfigurationEntity(
                    application.id,
                    creatorSub = sub,
                    name = "MainDto",
                    usedCommitHash = null,
                    streamKey = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )

        val found = configurationEntityRepository.findById(created.id)
        Assertions.assertEquals(created, found)
    }

    @Test
    fun removeById_positive_success() {
        val created =
            configurationEntityRepository.save(
                newConfigurationEntity(
                    application.id,
                    creatorSub = sub,
                    name = "MainDto",
                    usedCommitHash = null,
                    streamKey = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )

        val result = configurationEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = configurationEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = configurationEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val list = configurationEntityRepository.findAllByApplicationId(application.id)
        AssertionsJ.assertThat(list).isEmpty()
    }

    @Test
    fun removeById_nameIsFreed_success() {
        val templateEntity =
            newConfigurationEntity(
                application.id,
                creatorSub = sub,
                name = "MainDto",
                usedCommitHash = null,
                streamKey = null,
                schemaSourceType = SourceType.SERVICE,
            )

        val created1 = configurationEntityRepository.save(templateEntity)
        configurationEntityRepository.removeById(created1.id)

        Assertions.assertDoesNotThrow {
            configurationEntityRepository.save(templateEntity)
        }
    }
}
