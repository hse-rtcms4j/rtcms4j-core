package unit.ru.enzhine.rtcms4j.core.service.internal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.ApplicationManagerEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.service.external.KeycloakService
import ru.enzhine.rtcms4j.core.service.internal.ApplicationServiceImpl
import ru.enzhine.rtcms4j.core.service.internal.NamespaceService
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ApplicationServiceImplTest {
    @Mock
    lateinit var applicationEntityRepository: ApplicationEntityRepository

    @Mock
    lateinit var applicationManagerEntityRepository: ApplicationManagerEntityRepository

    @Spy
    val defaultPaginationProperties = DefaultPaginationProperties(10)

    @Mock
    lateinit var namespaceService: NamespaceService

    @Mock
    lateinit var keycloakService: KeycloakService

    @InjectMocks
    lateinit var applicationService: ApplicationServiceImpl

    private val uuid0 = UUID.fromString("9784ab00-fe10-4b8e-b402-e7ffb997cb39")
    private val uuid1 = UUID.fromString("9784abdb-fe10-4b8e-b402-e7ffb997cb39")
    private val uuid2 = UUID.fromString("638e4e64-2cc6-4142-a3f7-964e04b9ba5b")

    @Test
    fun createApplication_namespaceNotExists_error() {
        val creator = uuid0
        val namespaceId = 1L
        val name = "APP1"
        val description = "Some application"

        `when`(
            namespaceService.getNamespaceById(eq(namespaceId), any()),
        ).thenThrow(ConditionFailureException.NotFound::class.java)

        Assertions.assertThrows(ConditionFailureException.NotFound::class.java) {
            applicationService.createApplication(creator, namespaceId, name, description)
        }
    }

    @Test
    fun createApplication_namespaceExistsNewApplication_created() {
        val applicationId = 2L
        val creator = uuid0
        val namespaceId = 1L
        val name = "APP1"
        val description = "Some application"

        `when`(
            namespaceService.getNamespaceById(eq(namespaceId), any()),
        ).thenReturn(
            Namespace(
                id = namespaceId,
                name = "NS1",
                description = "Some application group",
            ),
        )

        `when`(
            applicationEntityRepository.save(
                argThat { it -> it.creatorSub == creator && it.name == name && it.description == description },
            ),
        ).thenReturn(
            ApplicationEntity(
                id = applicationId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                namespaceId = namespaceId,
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )

        val actual = applicationService.createApplication(creator, namespaceId, name, description)
        val expected =
            Application(
                id = applicationId,
                namespaceId = namespaceId,
                name = name,
                description = description,
            )
        Assertions.assertEquals(expected, actual)

        verify(keycloakService, times(1)).createNewApplicationClient(anyOrNull(), anyOrNull())
    }

    @Test
    fun createApplication_namespaceExistsApplicationRepeated_error() {
        val creator = uuid0
        val namespaceId = 1L
        val name = "APP1"
        val description = "Some application"

        `when`(
            namespaceService.getNamespaceById(eq(namespaceId), any()),
        ).thenReturn(
            Namespace(
                id = namespaceId,
                name = "NS1",
                description = "Some application group",
            ),
        )

        `when`(
            applicationEntityRepository.save(
                argThat { it -> it.creatorSub == creator && it.name == name && it.description == description },
            ),
        ).thenThrow(ConditionFailureException.KeyDuplicated::class.java)

        Assertions.assertThrows(ConditionFailureException.KeyDuplicated::class.java) {
            applicationService.createApplication(creator, namespaceId, name, description)
        }

        verify(keycloakService, never()).createNewApplicationClient(anyOrNull(), anyOrNull())
    }
}
