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
import ru.enzhine.rtcms4j.core.builder.newNamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import java.text.Collator
import java.util.Locale
import java.util.UUID
import kotlin.jvm.java
import org.assertj.core.api.Assertions as AssertionsJ

@SpringBootTest(
    classes = [
        NamespaceEntityRepositoryImpl::class,
        ApplicationEntityRepositoryImpl::class,
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
class ApplicationEntityRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    @Autowired
    private lateinit var applicationEntityRepository: ApplicationEntityRepositoryImpl

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")

    private lateinit var namespace: NamespaceEntity

    @BeforeEach
    fun clearTable() {
        jdbcTemplate.execute("truncate table namespace restart identity cascade;")
        jdbcTemplate.execute("truncate table application restart identity cascade;")

        namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports serving team",
                ),
            )
    }

    @Test
    fun save_positive_success() {
        val templateEntity =
            newApplicationEntity(
                namespaceId = namespace.id,
                creatorSub = sub,
                name = "Registry",
                description = "Clients data storage service",
                creationByService = true,
            )

        val created = applicationEntityRepository.save(templateEntity)
        Assertions.assertEquals(templateEntity.creatorSub, created.creatorSub)
        Assertions.assertEquals(templateEntity.namespaceId, created.namespaceId)
        Assertions.assertEquals(templateEntity.name, created.name)
        Assertions.assertEquals(templateEntity.description, created.description)
        Assertions.assertEquals(templateEntity.creationByService, created.creationByService)
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedName_error() {
        val templateEntity =
            newApplicationEntity(
                namespaceId = namespace.id,
                creatorSub = sub,
                name = "Registry",
                description = "Clients data storage service",
                creationByService = true,
            )

        applicationEntityRepository.save(templateEntity)
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            applicationEntityRepository.save(templateEntity)
        }
    }

    @Test
    fun save_namespaceDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            applicationEntityRepository.save(
                newApplicationEntity(
                    namespaceId = 10,
                    creatorSub = sub,
                    name = "Registry",
                    description = "Clients data storage service",
                    creationByService = true,
                ),
            )
        }
    }

    @Test
    fun update_positive_success() {
        val applicationEntity =
            applicationEntityRepository
                .save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Registry",
                        description = "Clients data storage service",
                        creationByService = true,
                    ),
                )

        val updated =
            applicationEntityRepository.update(
                applicationEntity.apply {
                    name = "Facade"
                    description = "Api gateway"
                },
            )
        Assertions.assertEquals(applicationEntity.name, updated.name)
        Assertions.assertEquals(applicationEntity.description, updated.description)
        Assertions.assertEquals(applicationEntity.id, updated.id)
    }

    @Test
    fun findAllByNamespaceIdAndName_positive_success() {
        val created =
            applicationEntityRepository.save(
                newApplicationEntity(
                    namespaceId = namespace.id,
                    creatorSub = sub,
                    name = "Registry",
                    description = "Clients data storage service",
                    creationByService = true,
                ),
            )

        val page = applicationEntityRepository.findAllByNamespaceIdAndName(namespace.id, null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, page.number)
        Assertions.assertEquals(2, page.size)
        Assertions.assertEquals(1, page.content.size)
        Assertions.assertEquals(created, page.content[0])
        Assertions.assertEquals(1, page.totalElements)
        Assertions.assertEquals(1, page.totalPages)
    }

    @Test
    fun findAllByNamespaceIdAndName_manyResultsByName_paginationCorrect() {
        val allValues =
            listOf(
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Registry",
                        description = "",
                        creationByService = true,
                    ),
                ),
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Dispatcher",
                        description = "",
                        creationByService = true,
                    ),
                ),
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Matcher",
                        description = "",
                        creationByService = true,
                    ),
                ),
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Validator",
                        description = "",
                        creationByService = true,
                    ),
                ),
            )

        val nonExistingPage = applicationEntityRepository.findAllByNamespaceIdAndName(10, null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, nonExistingPage.number)
        Assertions.assertEquals(2, nonExistingPage.size)
        Assertions.assertTrue(nonExistingPage.isEmpty)
        Assertions.assertEquals(0, nonExistingPage.totalElements)
        Assertions.assertEquals(0, nonExistingPage.totalPages)

        val emptyPage = applicationEntityRepository.findAllByNamespaceIdAndName(namespace.id, "Spring", PageRequest.of(0, 2))
        Assertions.assertEquals(0, emptyPage.number)
        Assertions.assertEquals(2, emptyPage.size)
        Assertions.assertTrue(emptyPage.isEmpty)
        Assertions.assertEquals(0, emptyPage.totalElements)
        Assertions.assertEquals(0, emptyPage.totalPages)

        val page0 = applicationEntityRepository.findAllByNamespaceIdAndName(namespace.id, "cher", PageRequest.of(0, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage0Content = allValues.filter { it.name.contains("cher") }
        Assertions.assertEquals(expectedPage0Content.size, page0.content.size)
        Assertions.assertTrue(page0.content.containsAll(expectedPage0Content))
        Assertions.assertEquals(expectedPage0Content.size.toLong(), page0.totalElements)
        Assertions.assertEquals(1, page0.totalPages)
    }

    @Test
    fun findAllByNamespaceIdAndName_manyResults_paginationCorrect() {
        val collator = Collator.getInstance(Locale.US)

        val allValues =
            listOf(
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Registry",
                        description = "",
                        creationByService = true,
                    ),
                ),
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Dispatcher",
                        description = "",
                        creationByService = true,
                    ),
                ),
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Matcher",
                        description = "",
                        creationByService = true,
                    ),
                ),
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = sub,
                        name = "Validator",
                        description = "",
                        creationByService = true,
                    ),
                ),
            ).sortedWith(compareBy(collator) { it.name })

        val page0 = applicationEntityRepository.findAllByNamespaceIdAndName(namespace.id, null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage0Content = allValues.subList(0, 2)
        Assertions.assertEquals(expectedPage0Content.size, page0.content.size)
        AssertionsJ.assertThat(page0.content).containsAll(expectedPage0Content)
        Assertions.assertEquals(allValues.size.toLong(), page0.totalElements)
        Assertions.assertEquals(2, page0.totalPages)

        val page1 = applicationEntityRepository.findAllByNamespaceIdAndName(namespace.id, null, PageRequest.of(1, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage1Content = allValues.subList(2, 4)
        Assertions.assertEquals(expectedPage1Content.size, page1.content.size)
        Assertions.assertTrue(page1.content.containsAll(expectedPage1Content))
        Assertions.assertEquals(allValues.size.toLong(), page0.totalElements)
        Assertions.assertEquals(2, page0.totalPages)
    }

    @Test
    fun findById_positive_success() {
        val created =
            applicationEntityRepository.save(
                newApplicationEntity(
                    namespaceId = namespace.id,
                    creatorSub = sub,
                    name = "Registry",
                    description = "Clients data storage service",
                    creationByService = true,
                ),
            )
        val found = applicationEntityRepository.findById(created.id)

        Assertions.assertEquals(created, found)
    }

    @Test
    fun removeById_positive_success() {
        val created =
            applicationEntityRepository.save(
                newApplicationEntity(
                    namespaceId = namespace.id,
                    creatorSub = sub,
                    name = "Registry",
                    description = "Clients data storage service",
                    creationByService = true,
                ),
            )
        val result = applicationEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = applicationEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = applicationEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val page = applicationEntityRepository.findAllByNamespaceIdAndName(namespace.id, null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, page.number)
        Assertions.assertEquals(2, page.size)
        Assertions.assertEquals(0, page.content.size)
        Assertions.assertEquals(0, page.content.size)
        Assertions.assertEquals(0, page.totalElements)
        Assertions.assertEquals(0, page.totalPages)
    }

    @Test
    fun removeById_nameIsFreed_success() {
        val templateEntity =
            newApplicationEntity(
                namespaceId = namespace.id,
                creatorSub = sub,
                name = "Registry",
                description = "Clients data storage service",
                creationByService = true,
            )

        val created1 = applicationEntityRepository.save(templateEntity)
        applicationEntityRepository.removeById(created1.id)

        Assertions.assertDoesNotThrow {
            applicationEntityRepository.save(templateEntity)
        }
    }
}
