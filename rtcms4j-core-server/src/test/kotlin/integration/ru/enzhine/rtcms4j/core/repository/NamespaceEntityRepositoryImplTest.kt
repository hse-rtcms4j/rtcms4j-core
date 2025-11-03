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
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.enzhine.rtcms4j.core.repository.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.dto.newNamespaceEntity
import java.util.UUID
import kotlin.jvm.java

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
    fun findAllByName_nameProvided_success() {
        val created1 =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports serving team",
                ),
            )

        val created2 =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BPH Team",
                    description = "Broker data allocation team",
                ),
            )

        val allPage = namespaceEntityRepository.findAllByName(null, PageRequest.of(0, 2))
        Assertions.assertEquals(0, allPage.number)
        Assertions.assertEquals(2, allPage.size)
        Assertions.assertEquals(2, allPage.content.size)
        Assertions.assertTrue(allPage.content.containsAll(listOf(created1, created2)))
        Assertions.assertEquals(2, allPage.totalElements)
        Assertions.assertEquals(1, allPage.totalPages)

        val bphPage = namespaceEntityRepository.findAllByName("BPH", PageRequest.of(0, 2))
        Assertions.assertEquals(0, bphPage.number)
        Assertions.assertEquals(2, bphPage.size)
        Assertions.assertEquals(1, bphPage.content.size)
        Assertions.assertEquals(created2, bphPage.content[0])
        Assertions.assertEquals(1, bphPage.totalElements)
        Assertions.assertEquals(1, bphPage.totalPages)
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
