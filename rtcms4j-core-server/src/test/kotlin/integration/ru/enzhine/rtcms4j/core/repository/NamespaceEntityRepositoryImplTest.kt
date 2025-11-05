package integration.ru.enzhine.rtcms4j.core.repository

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.enzhine.rtcms4j.core.repository.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.dto.newNamespaceEntity
import java.util.UUID
import kotlin.jvm.java
import org.assertj.core.api.Assertions as AssertionsJ

@SpringBootTest(classes = [NamespaceEntityRepositoryImpl::class])
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
class NamespaceEntityRepositoryImplTest {
    private val logger = LoggerFactory.getLogger(NamespaceEntityRepositoryImplTest::class.java)

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")

    @BeforeEach
    fun clearTable() {
        jdbcTemplate.execute("truncate table namespace restart identity cascade;")
    }

    @Test
    fun save_positive_success() {
        val templateEntity =
            newNamespaceEntity(
                creatorSub = sub,
                name = "BCR Team",
                description = "Broker reports serving team",
            )

        val created = namespaceEntityRepository.save(templateEntity)
        Assertions.assertEquals(sub, created.creatorSub)
        Assertions.assertEquals(templateEntity.name, created.name)
        Assertions.assertEquals(templateEntity.description, created.description)
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedName_error() {
        val name = "BCR Team"

        namespaceEntityRepository.save(
            newNamespaceEntity(
                creatorSub = sub,
                name = name,
                description = "Broker reports serving team",
            ),
        )

        Assertions.assertThrows(DuplicateKeyException::class.java) {
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = name,
                    description = "Very friendly developers team",
                ),
            )
        }
    }

    @Test
    fun findAllByName_positive_success() {
        val templateEntity =
            newNamespaceEntity(
                creatorSub = sub,
                name = "BCR Team",
                description = "Broker reports serving team",
            )

        val created = namespaceEntityRepository.save(templateEntity)

        val page = namespaceEntityRepository.findAllByName(null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, page.number)
        Assertions.assertEquals(2, page.size)
        Assertions.assertEquals(1, page.content.size)
        Assertions.assertEquals(created, page.content[0])
        Assertions.assertEquals(1, page.totalElements)
        Assertions.assertEquals(1, page.totalPages)
    }

    @Test
    fun findAllByName_manyResultsByName_paginationCorrect() {
        val allValues =
            listOf(
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "Apple",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "Banana",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "BCR Team",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "BPH Team",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "Cake",
                        description = "",
                    ),
                ),
            )

        val emptyPage = namespaceEntityRepository.findAllByName("Spring", PageRequest.of(0, 2))
        Assertions.assertEquals(0, emptyPage.number)
        Assertions.assertEquals(2, emptyPage.size)
        Assertions.assertTrue(emptyPage.isEmpty)
        Assertions.assertEquals(0, emptyPage.totalElements)
        Assertions.assertEquals(0, emptyPage.totalPages)

        val page0 = namespaceEntityRepository.findAllByName("Apple", PageRequest.of(0, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage0Content = allValues.filter { it.name.contains("Apple") }
        Assertions.assertEquals(expectedPage0Content.size, page0.content.size)
        Assertions.assertTrue(page0.content.containsAll(expectedPage0Content))
        Assertions.assertEquals(expectedPage0Content.size.toLong(), page0.totalElements)
        Assertions.assertEquals(1, page0.totalPages)
    }

    @Test
    fun findAllByName_manyResults_paginationCorrect() {
        val allValues =
            listOf(
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "Apple",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "Banana",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "BCR Team",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "BPH Team",
                        description = "",
                    ),
                ),
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = sub,
                        name = "Cake",
                        description = "",
                    ),
                ),
            )

        logger.warn("All created: " + namespaceEntityRepository.findAllByName(null, PageRequest.of(0, 6)).joinToString(","))

        val page0 = namespaceEntityRepository.findAllByName(null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage0Content = allValues.subList(0, 2)
        Assertions.assertEquals(expectedPage0Content.size, page0.content.size)
        AssertionsJ.assertThat(page0.content).containsAll(expectedPage0Content)
        Assertions.assertEquals(allValues.size.toLong(), page0.totalElements)
        Assertions.assertEquals(3, page0.totalPages)

        val page1 = namespaceEntityRepository.findAllByName(null, PageRequest.of(1, 2))
        Assertions.assertEquals(0, page0.number)
        Assertions.assertEquals(2, page0.size)
        val expectedPage1Content = allValues.subList(2, 4)
        Assertions.assertEquals(expectedPage1Content.size, page1.content.size)
        Assertions.assertTrue(page1.content.containsAll(expectedPage1Content))
        Assertions.assertEquals(allValues.size.toLong(), page0.totalElements)
        Assertions.assertEquals(3, page0.totalPages)
    }

    @Test
    fun findById_positive_success() {
        val templateEntity =
            newNamespaceEntity(
                creatorSub = sub,
                name = "BCR Team",
                description = "Broker reports serving team",
            )

        val created = namespaceEntityRepository.save(templateEntity)
        val found = namespaceEntityRepository.findById(created.id)

        Assertions.assertEquals(created, found)
    }

    @Test
    fun removeById_positive_success() {
        val templateEntity =
            newNamespaceEntity(
                creatorSub = sub,
                name = "BCR Team",
                description = "Broker reports serving team",
            )

        val created = namespaceEntityRepository.save(templateEntity)
        val result = namespaceEntityRepository.removeById(created.id)
        Assertions.assertTrue(result)
        val result2 = namespaceEntityRepository.removeById(created.id)
        Assertions.assertFalse(result2)

        val found = namespaceEntityRepository.findById(created.id)
        Assertions.assertNull(found)

        val page = namespaceEntityRepository.findAllByName(null, PageRequest.of(0, 2))
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
            newNamespaceEntity(
                creatorSub = sub,
                name = "BCR Team",
                description = "Broker reports serving team",
            )
        val created1 = namespaceEntityRepository.save(templateEntity)
        namespaceEntityRepository.removeById(created1.id)

        val created2 = namespaceEntityRepository.save(templateEntity)
        namespaceEntityRepository.removeById(created1.id)
        val found = namespaceEntityRepository.findById(created2.id)
        Assertions.assertEquals(created2, found)
    }
}
