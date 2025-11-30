package integration.ru.enzhine.rtcms4j.core.repository.db

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
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.enzhine.rtcms4j.core.builder.newApplicationEntity
import ru.enzhine.rtcms4j.core.builder.newConfigurationEntity
import ru.enzhine.rtcms4j.core.builder.newNamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.ConfigurationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType
import java.text.Collator
import java.util.Locale
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
                commitHash = null,
                schemaSourceType = SourceType.SERVICE,
            )

        val created = configurationEntityRepository.save(templateEntity)
        Assertions.assertEquals(templateEntity.creatorSub, created.creatorSub)
        Assertions.assertEquals(templateEntity.applicationId, created.applicationId)
        Assertions.assertEquals(templateEntity.name, created.name)
        Assertions.assertEquals(templateEntity.commitHash, created.commitHash)
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
                commitHash = null,
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
                    commitHash = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )
        }
    }

    @Test
    fun update_positive_success() {
        val configurationEntity =
            configurationEntityRepository
                .save(
                    newConfigurationEntity(
                        application.id,
                        creatorSub = sub,
                        name = "MainDto",
                        commitHash = null,
                        schemaSourceType = SourceType.SERVICE,
                    ),
                ).apply {
                    name = "Facade"
                    schemaSourceType = SourceType.USER
                    commitHash = "abcd1234"
                }

        val updated = configurationEntityRepository.update(configurationEntity)
        Assertions.assertEquals(configurationEntity.name, updated.name)
        Assertions.assertEquals(configurationEntity.schemaSourceType, updated.schemaSourceType)
        Assertions.assertEquals(configurationEntity.commitHash, updated.commitHash)
        Assertions.assertEquals(configurationEntity.id, updated.id)
    }

    @Test
    fun findAllByApplicationIdAndName_positive_success() {
        val created =
            configurationEntityRepository.save(
                newConfigurationEntity(
                    application.id,
                    creatorSub = sub,
                    name = "MainDto",
                    commitHash = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )

        val page = configurationEntityRepository.findAllByApplicationIdAndName(application.id, null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, page.number)
        Assertions.assertEquals(2, page.size)
        Assertions.assertEquals(1, page.content.size)
        Assertions.assertEquals(created, page.content[0])
        Assertions.assertEquals(1, page.totalElements)
        Assertions.assertEquals(1, page.totalPages)
    }

    @Test
    fun findAllByApplicationIdAndName_manyResultsByName_paginationCorrect() {
        val allValues =
            listOf(
                configurationEntityRepository.save(
                    newConfigurationEntity(
                        application.id,
                        creatorSub = sub,
                        name = "MainDto",
                        commitHash = null,
                        schemaSourceType = SourceType.SERVICE,
                    ),
                ),
                configurationEntityRepository.save(
                    newConfigurationEntity(
                        application.id,
                        creatorSub = sub,
                        name = "BasicDto",
                        commitHash = null,
                        schemaSourceType = SourceType.SERVICE,
                    ),
                ),
                configurationEntityRepository.save(
                    newConfigurationEntity(
                        application.id,
                        creatorSub = sub,
                        name = "RandomDto",
                        commitHash = null,
                        schemaSourceType = SourceType.SERVICE,
                    ),
                ),
            )

        val nonExistingPage = configurationEntityRepository.findAllByApplicationIdAndName(10, null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, nonExistingPage.number)
        Assertions.assertEquals(2, nonExistingPage.size)
        Assertions.assertTrue(nonExistingPage.isEmpty)
        Assertions.assertEquals(0, nonExistingPage.totalElements)
        Assertions.assertEquals(0, nonExistingPage.totalPages)

        val emptyPage = configurationEntityRepository.findAllByApplicationIdAndName(application.id, "Crucial", PageRequest.of(0, 2))
        Assertions.assertEquals(0, emptyPage.number)
        Assertions.assertEquals(2, emptyPage.size)
        Assertions.assertTrue(emptyPage.isEmpty)
        Assertions.assertEquals(0, emptyPage.totalElements)
        Assertions.assertEquals(0, emptyPage.totalPages)

        val page0 = configurationEntityRepository.findAllByApplicationIdAndName(application.id, "Random", PageRequest.of(0, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage0Content = allValues.filter { it.name.contains("Random") }
        Assertions.assertEquals(expectedPage0Content.size, page0.content.size)
        Assertions.assertTrue(page0.content.containsAll(expectedPage0Content))
        Assertions.assertEquals(expectedPage0Content.size.toLong(), page0.totalElements)
        Assertions.assertEquals(1, page0.totalPages)
    }

    @Test
    fun findAllByApplicationIdAndName_manyResults_paginationCorrect() {
        val collator = Collator.getInstance(Locale.US)

        val allValues =
            listOf(
                configurationEntityRepository.save(
                    newConfigurationEntity(
                        application.id,
                        creatorSub = sub,
                        name = "DtoC",
                        commitHash = null,
                        schemaSourceType = SourceType.SERVICE,
                    ),
                ),
                configurationEntityRepository.save(
                    newConfigurationEntity(
                        application.id,
                        creatorSub = sub,
                        name = "DtoA",
                        commitHash = null,
                        schemaSourceType = SourceType.SERVICE,
                    ),
                ),
                configurationEntityRepository.save(
                    newConfigurationEntity(
                        application.id,
                        creatorSub = sub,
                        name = "DtoB",
                        commitHash = null,
                        schemaSourceType = SourceType.SERVICE,
                    ),
                ),
            ).sortedWith(compareBy(collator) { it.name })

        val page0 = configurationEntityRepository.findAllByApplicationIdAndName(application.id, null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage0Content = allValues.subList(0, 2)
        Assertions.assertEquals(expectedPage0Content.size, page0.content.size)
        AssertionsJ.assertThat(page0.content).containsAll(expectedPage0Content)
        Assertions.assertEquals(allValues.size.toLong(), page0.totalElements)
        Assertions.assertEquals(2, page0.totalPages)

        val page1 = configurationEntityRepository.findAllByApplicationIdAndName(namespace.id, null, PageRequest.of(1, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage1Content = allValues.subList(2, allValues.size)
        Assertions.assertEquals(expectedPage1Content.size, page1.content.size)
        Assertions.assertTrue(page1.content.containsAll(expectedPage1Content))
        Assertions.assertEquals(allValues.size.toLong(), page0.totalElements)
        Assertions.assertEquals(2, page0.totalPages)
    }

    @Test
    fun findById_positive_success() {
        val created =
            configurationEntityRepository.save(
                newConfigurationEntity(
                    application.id,
                    creatorSub = sub,
                    name = "MainDto",
                    commitHash = null,
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
                    commitHash = null,
                    schemaSourceType = SourceType.SERVICE,
                ),
            )

        val result = configurationEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = configurationEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = configurationEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val list = configurationEntityRepository.findAllByApplicationIdAndName(application.id, null, PageRequest.of(0, 2))
        AssertionsJ.assertThat(list).isEmpty()
    }

    @Test
    fun removeById_nameIsFreed_success() {
        val templateEntity =
            newConfigurationEntity(
                application.id,
                creatorSub = sub,
                name = "MainDto",
                commitHash = null,
                schemaSourceType = SourceType.SERVICE,
            )

        val created1 = configurationEntityRepository.save(templateEntity)
        configurationEntityRepository.removeById(created1.id)

        Assertions.assertDoesNotThrow {
            configurationEntityRepository.save(templateEntity)
        }
    }
}
