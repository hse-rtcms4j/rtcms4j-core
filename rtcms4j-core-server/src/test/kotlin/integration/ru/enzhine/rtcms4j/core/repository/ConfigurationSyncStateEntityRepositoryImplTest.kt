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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.enzhine.rtcms4j.core.repository.ApplicationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.ConfigurationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.ConfigurationSyncStateEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import ru.enzhine.rtcms4j.core.repository.dto.newApplicationEntity
import ru.enzhine.rtcms4j.core.repository.dto.newConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.dto.newConfigurationSyncStateEntity
import ru.enzhine.rtcms4j.core.repository.dto.newNamespaceEntity
import java.util.UUID
import kotlin.jvm.java
import org.assertj.core.api.Assertions as AssertionsJ

@SpringBootTest(
    classes = [
        NamespaceEntityRepositoryImpl::class,
        ApplicationEntityRepositoryImpl::class,
        ConfigurationEntityRepositoryImpl::class,
        ConfigurationSyncStateEntityRepositoryImpl::class,
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
class ConfigurationSyncStateEntityRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    @Autowired
    private lateinit var applicationEntityRepository: ApplicationEntityRepositoryImpl

    @Autowired
    private lateinit var configurationEntityRepository: ConfigurationEntityRepositoryImpl

    @Autowired
    private lateinit var configurationSyncStateEntityRepository: ConfigurationSyncStateEntityRepositoryImpl

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")
    private lateinit var namespace: NamespaceEntity
    private lateinit var application: ApplicationEntity
    private lateinit var configuration: ConfigurationEntity

    @BeforeEach
    fun clearTable() {
        jdbcTemplate.execute("truncate table namespace restart identity cascade;")
        jdbcTemplate.execute("truncate table application restart identity cascade;")
        jdbcTemplate.execute("truncate table configuration restart identity cascade;")
        jdbcTemplate.execute("truncate table configuration_sync_state restart identity cascade;")

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

    @Test
    fun save_positive_success() {
        val templateEntity =
            newConfigurationSyncStateEntity(
                configuration.id,
                sourceIdentity = "App-1",
                commitHash = "a1b1",
                isOnline = true,
            )

        val created = configurationSyncStateEntityRepository.save(templateEntity)
        Assertions.assertEquals(templateEntity.configurationId, created.configurationId)
        Assertions.assertEquals(templateEntity.sourceIdentity, created.sourceIdentity)
        Assertions.assertEquals(templateEntity.commitHash, created.commitHash)
        Assertions.assertEquals(templateEntity.isOnline, created.isOnline)
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_configurationDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            configurationSyncStateEntityRepository.save(
                newConfigurationSyncStateEntity(
                    10,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    isOnline = true,
                ),
            )
        }
    }

    @Test
    fun findAllByConfigurationIdAndAfterTimestamp_positive_success() {
        val createdBefore =
            configurationSyncStateEntityRepository.save(
                newConfigurationSyncStateEntity(
                    configuration.id,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    isOnline = null,
                ),
            )
        Thread.sleep(500)
        val createdAfter =
            listOf(
                configurationSyncStateEntityRepository.save(
                    newConfigurationSyncStateEntity(
                        configuration.id,
                        sourceIdentity = "App-1",
                        commitHash = "a1b1",
                        isOnline = null,
                    ),
                ),
                configurationSyncStateEntityRepository.save(
                    newConfigurationSyncStateEntity(
                        configuration.id,
                        sourceIdentity = "App-1",
                        commitHash = "a1b1",
                        isOnline = null,
                    ),
                ),
            )
        val timestamp = createdAfter[0].createdAt

        val list =
            configurationSyncStateEntityRepository.findAllByConfigurationIdAndAfterTimestamp(
                configuration.id,
                timestamp,
            )
        AssertionsJ.assertThat(list).doesNotContain(createdBefore)
        AssertionsJ.assertThat(list).containsAll(createdAfter)
    }

    @Test
    fun findById_positive_success() {
        val created =
            configurationSyncStateEntityRepository.save(
                newConfigurationSyncStateEntity(
                    configuration.id,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    isOnline = null,
                ),
            )

        val found = configurationSyncStateEntityRepository.findById(created.id)
        Assertions.assertEquals(created, found)
    }

    @Test
    fun removeById_positive_success() {
        val created =
            configurationSyncStateEntityRepository.save(
                newConfigurationSyncStateEntity(
                    configuration.id,
                    sourceIdentity = "App-1",
                    commitHash = "a1b1",
                    isOnline = null,
                ),
            )

        val result = configurationSyncStateEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = configurationSyncStateEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = configurationSyncStateEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val list =
            configurationSyncStateEntityRepository.findAllByConfigurationIdAndAfterTimestamp(
                created.configurationId,
                created.createdAt,
            )
        AssertionsJ.assertThat(list).isEmpty()
    }
}
