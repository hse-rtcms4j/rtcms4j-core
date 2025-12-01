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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.enzhine.rtcms4j.core.builder.newApplicationEntity
import ru.enzhine.rtcms4j.core.builder.newApplicationManagerEntity
import ru.enzhine.rtcms4j.core.builder.newNamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.ApplicationManagerEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepositoryImpl
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import java.util.UUID
import kotlin.jvm.java
import org.assertj.core.api.Assertions as AssertionsJ

@SpringBootTest(
    classes = [
        NamespaceEntityRepositoryImpl::class,
        ApplicationEntityRepositoryImpl::class,
        ApplicationManagerEntityRepositoryImpl::class,
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
class ApplicationManagerEntityRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namespaceEntityRepository: NamespaceEntityRepositoryImpl

    @Autowired
    private lateinit var applicationEntityRepository: ApplicationEntityRepositoryImpl

    @Autowired
    private lateinit var applicationManagerEntityRepository: ApplicationManagerEntityRepositoryImpl

    private val sub = UUID.fromString("fb9fff20-52d8-4fa0-9b24-35a85303e70b")
    private val accessToken = "kashdvn817t17envoaidjjvna75as65aios9y"

    private lateinit var namespace: NamespaceEntity
    private lateinit var application: ApplicationEntity

    @BeforeEach
    fun clearTableAndInit() {
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

        application =
            applicationEntityRepository.save(
                newApplicationEntity(
                    namespaceId = namespace.id,
                    creatorSub = sub,
                    name = "Registry",
                    description = "Clients data storage service",
                ),
            )
    }

    @Test
    fun save_positive_success() {
        val managerEntity =
            newApplicationManagerEntity(
                applicationId = application.id,
                assignerSub = sub,
                userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
            )

        val created = applicationManagerEntityRepository.save(managerEntity)
        Assertions.assertEquals(managerEntity.applicationId, created.applicationId)
        Assertions.assertEquals(managerEntity.assignerSub, created.assignerSub)
        Assertions.assertEquals(managerEntity.userSub, created.userSub)
        Assertions.assertEquals(1, created.id)
    }

    @Test
    fun save_repeatedAttempt_success() {
        val templateEntity =
            newApplicationManagerEntity(
                applicationId = application.id,
                assignerSub = sub,
                userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
            )

        applicationManagerEntityRepository.save(templateEntity)
        Assertions.assertThrows(DuplicateKeyException::class.java) {
            applicationManagerEntityRepository.save(templateEntity)
        }
    }

    @Test
    fun save_applicationDoesNotExist_error() {
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = 10,
                    assignerSub = sub,
                    userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
                ),
            )
        }
    }

    @Test
    fun findAllByApplicationId_positive_success() {
        val manager1 =
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = application.id,
                    assignerSub = sub,
                    userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
                ),
            )
        val manager2 =
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = application.id,
                    assignerSub = sub,
                    userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e711"),
                ),
            )

        val managers = applicationManagerEntityRepository.findAllByApplicationId(application.id)
        Assertions.assertEquals(2, managers.size)
        AssertionsJ.assertThat(managers).contains(manager1, manager2)
    }

    @Test
    fun findByApplicationIdAndUserSub_defaultBehavior_success() {
        val user1Sub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b")
        val user2Sub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e711")

        val manager1 =
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = application.id,
                    assignerSub = sub,
                    userSub = user1Sub,
                ),
            )

        val foundManager = applicationManagerEntityRepository.findByApplicationIdAndUserSub(application.id, user1Sub)
        Assertions.assertEquals(manager1, foundManager)

        val foundNone = applicationManagerEntityRepository.findByApplicationIdAndUserSub(application.id, user2Sub)
        Assertions.assertNull(foundNone)
    }

    @Test
    fun findById_positive_success() {
        val manager =
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = application.id,
                    assignerSub = sub,
                    userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b"),
                ),
            )

        val foundRecord = applicationManagerEntityRepository.findById(manager.id)
        Assertions.assertEquals(manager, foundRecord)
    }

    @Test
    fun removeById_defaultBehavior_success() {
        val userSub = UUID.fromString("449fff20-52d8-4fa0-9b24-35a85303e70b")
        val manager =
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = application.id,
                    assignerSub = sub,
                    userSub = userSub,
                ),
            )

        val result = applicationManagerEntityRepository.removeById(manager.id)
        Assertions.assertTrue(result)

        val foundNone = applicationManagerEntityRepository.findByApplicationIdAndUserSub(application.id, userSub)
        Assertions.assertNull(foundNone)

        Assertions.assertDoesNotThrow {
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = application.id,
                    assignerSub = sub,
                    userSub = userSub,
                ),
            )
        }
    }
}
