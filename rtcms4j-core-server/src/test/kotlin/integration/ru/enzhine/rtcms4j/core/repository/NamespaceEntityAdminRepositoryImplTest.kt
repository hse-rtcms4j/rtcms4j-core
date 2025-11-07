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
import ru.enzhine.rtcms4j.core.repository.NamespaceAdminEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.dto.newNamespaceAdminEntity
import ru.enzhine.rtcms4j.core.repository.dto.newNamespaceEntity
import java.util.UUID
import kotlin.jvm.java

@SpringBootTest(
    classes = [
        NamespaceEntityRepositoryImpl::class,
        NamespaceAdminEntityRepositoryImpl::class,
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
class NamespaceEntityAdminRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    @Autowired
    private lateinit var namespaceAdminEntityRepository: NamespaceAdminEntityRepositoryImpl

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")

    @BeforeEach
    fun clearTable() {
        jdbcTemplate.execute("truncate table namespace restart identity cascade;")
        jdbcTemplate.execute("truncate table namespace_admin restart identity cascade;")
    }

    @Test
    fun save_positive_success() {
        val namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports serving team",
                ),
            )

        val adminEntity =
            newNamespaceAdminEntity(
                namespaceId = namespace.id,
                assignerSub = sub,
                userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
            )
        val created = namespaceAdminEntityRepository.save(adminEntity)
        Assertions.assertEquals(adminEntity.namespaceId, created.namespaceId)
        Assertions.assertEquals(adminEntity.assignerSub, created.assignerSub)
        Assertions.assertEquals(adminEntity.userSub, created.userSub)
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedAttempt_success() {
        val namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports serving team",
                ),
            )

        val userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b")
        namespaceAdminEntityRepository.save(
            newNamespaceAdminEntity(
                namespaceId = namespace.id,
                assignerSub = sub,
                userSub = userSub,
            ),
        )
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespace.id,
                    assignerSub = sub,
                    userSub = userSub,
                ),
            )
        }
    }

    @Test
    fun save_namespaceDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = 10,
                    assignerSub = sub,
                    userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
                ),
            )
        }
    }

    @Test
    fun findAllByNamespaceId_positive_success() {
        val namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports production and delivery team",
                ),
            )

        val user1Sub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b")
        val user2Sub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e711")

        val admin1 =
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespace.id,
                    assignerSub = sub,
                    userSub = user1Sub,
                ),
            )
        val admin2 =
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespace.id,
                    assignerSub = sub,
                    userSub = user2Sub,
                ),
            )

        val admins = namespaceAdminEntityRepository.findAllByNamespaceId(namespace.id)
        Assertions.assertEquals(2, admins.size)
        Assertions.assertTrue(admins.contains(admin1))
        Assertions.assertTrue(admins.contains(admin2))
    }

    @Test
    fun findByNamespaceIdAndUserSub_defaultBehavior_success() {
        val namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports production and delivery team",
                ),
            )

        val user1Sub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b")
        val user2Sub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e711")

        val admin1 =
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespace.id,
                    assignerSub = sub,
                    userSub = user1Sub,
                ),
            )

        val foundAdmin = namespaceAdminEntityRepository.findByNamespaceIdAndUserSub(namespace.id, user1Sub)
        Assertions.assertEquals(admin1, foundAdmin)

        val foundNone = namespaceAdminEntityRepository.findByNamespaceIdAndUserSub(namespace.id, user2Sub)
        Assertions.assertNull(foundNone)
    }

    @Test
    fun findById_positive_success() {
        val namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports production and delivery team",
                ),
            )

        val admin =
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespace.id,
                    assignerSub = sub,
                    userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
                ),
            )

        val foundRecord = namespaceAdminEntityRepository.findById(admin.id)
        Assertions.assertEquals(admin, foundRecord)
    }

    @Test
    fun removeById_defaultBehavior_success() {
        val namespace =
            namespaceEntityRepository.save(
                newNamespaceEntity(
                    creatorSub = sub,
                    name = "BCR Team",
                    description = "Broker reports production and delivery team",
                ),
            )

        val userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b")
        val admin =
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespace.id,
                    assignerSub = sub,
                    userSub = userSub,
                ),
            )

        val result = namespaceAdminEntityRepository.removeById(admin.id)
        Assertions.assertTrue(result)

        val foundNone = namespaceAdminEntityRepository.findByNamespaceIdAndUserSub(namespace.id, userSub)
        Assertions.assertNull(foundNone)

        Assertions.assertDoesNotThrow {
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespace.id,
                    assignerSub = sub,
                    userSub = userSub,
                ),
            )
        }
    }
}
