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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.repository.db.NamespaceAdminEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.NamespaceEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceAdminEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.service.external.KeycloakService
import ru.enzhine.rtcms4j.core.service.internal.NamespaceServiceImpl
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import java.time.OffsetDateTime
import java.util.UUID
import org.assertj.core.api.Assertions as AssertionsJ

@ExtendWith(MockitoExtension::class)
class NamespaceServiceImplTest {
    @Mock
    lateinit var namespaceEntityRepository: NamespaceEntityRepository

    @Mock
    lateinit var namespaceAdminEntityRepository: NamespaceAdminEntityRepository

    @Mock
    lateinit var keycloakService: KeycloakService

    @Spy
    val defaultPaginationProperties = DefaultPaginationProperties(10)

    @InjectMocks
    lateinit var namespaceService: NamespaceServiceImpl

    private val uuid0 = UUID.fromString("9784ab00-fe10-4b8e-b402-e7ffb997cb39")
    private val uuid1 = UUID.fromString("9784abdb-fe10-4b8e-b402-e7ffb997cb39")
    private val uuid2 = UUID.fromString("638e4e64-2cc6-4142-a3f7-964e04b9ba5b")

    @Test
    fun createNamespace_new_created() {
        val namespaceId = 1L
        val creator = uuid1
        val name = "NS1"
        val description = "Some application group"

        `when`(
            namespaceEntityRepository.save(
                argThat { it -> it.creatorSub == creator && it.name == name && it.description == description },
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )
        val actual = namespaceService.createNamespace(creator, name, description)

        val expected =
            Namespace(
                id = namespaceId,
                name = name,
                description = description,
            )
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun createNamespace_alreadyExists_error() {
        val creator = uuid1
        val name = "NS1"
        val description = "Some application group"

        `when`(
            namespaceEntityRepository.save(
                argThat { it -> it.creatorSub == creator && it.name == name && it.description == description },
            ),
        ).thenThrow(DuplicateKeyException::class.java)

        Assertions.assertThrows(ConditionFailureException.KeyDuplicated::class.java) {
            namespaceService.createNamespace(creator, name, description)
        }
    }

    @Test
    fun getNamespaceById_exists_found() {
        val namespaceId = 1L
        val creator = uuid1
        val name = "NS1"
        val description = "Some application group"

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )
        val actual = namespaceService.getNamespaceById(namespaceId, false)

        val expected =
            Namespace(
                id = namespaceId,
                name = name,
                description = description,
            )
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun getNamespaceById_notExists_null() {
        val namespaceId = 1L

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(null)

        Assertions.assertThrows(ConditionFailureException.NotFound::class.java) {
            namespaceService.getNamespaceById(namespaceId, false)
        }
    }

    @Test
    fun updateNamespace_notExistsNoUpdate_null() {
        val namespaceId = 1L

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(null)

        Assertions.assertThrows(ConditionFailureException.NotFound::class.java) {
            namespaceService.updateNamespace(namespaceId, null, null)
        }

        verify(namespaceEntityRepository, never()).update(any())
    }

    @Test
    fun updateNamespace_existsNoUpdate_same() {
        val namespaceId = 1L
        val creator = uuid1
        val name = "NS1"
        val description = "Some application group"

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )
        val actual = namespaceService.updateNamespace(namespaceId, null, null)

        verify(namespaceEntityRepository, never()).update(any())

        val expected =
            Namespace(
                id = namespaceId,
                name = name,
                description = description,
            )
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun updateNamespace_existsUpdated_updated() {
        val namespaceId = 1L
        val creator = uuid1
        val name = "NS1"
        val description = "Some application group"
        val nameToUpdate = "NS2"
        val descriptionToUpdate = "Some application group as well"

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )
        `when`(
            namespaceEntityRepository.update(
                argThat { it -> it.id == namespaceId && it.name == nameToUpdate && it.description == descriptionToUpdate },
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = nameToUpdate,
                description = descriptionToUpdate,
            ),
        )
        val actual = namespaceService.updateNamespace(namespaceId, nameToUpdate, descriptionToUpdate)

        val expected =
            Namespace(
                id = namespaceId,
                name = nameToUpdate,
                description = descriptionToUpdate,
            )
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun findNamespaces_nameProvided_found() {
        val namespaceId = 1L
        val creator = uuid1
        val name = "NS1"
        val description = "Some application group"

        val nameRequest = name
        val pageableRequest = PageRequest.of(0, 10)

        `when`(
            namespaceEntityRepository.findAllByName(nameRequest, pageableRequest),
        ).thenReturn(
            PageImpl(
                listOf(
                    NamespaceEntity(
                        id = namespaceId,
                        createdAt = OffsetDateTime.now(),
                        updatedAt = OffsetDateTime.now(),
                        creatorSub = creator,
                        name = name,
                        description = description,
                    ),
                ),
                pageableRequest,
                1,
            ),
        )
        val actual = namespaceService.findNamespaces(nameRequest, pageableRequest)

        val expected =
            PageImpl(
                listOf(
                    Namespace(
                        id = namespaceId,
                        name = name,
                        description = description,
                    ),
                ),
                pageableRequest,
                1,
            )
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun deleteNamespace_notDeleted_false() {
        val namespaceId = 1L

        `when`(namespaceEntityRepository.removeById(namespaceId)).thenReturn(false)
        val actual = namespaceService.deleteNamespace(namespaceId)

        val expected = false
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun deleteNamespace_deleted_true() {
        val namespaceId = 1L

        `when`(namespaceEntityRepository.removeById(namespaceId)).thenReturn(true)
        val actual = namespaceService.deleteNamespace(namespaceId)

        val expected = true
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun listAdmins_notExistsNoAdmins_empty() {
        val namespaceId = 1L

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(null)

        Assertions.assertThrows(ConditionFailureException.NotFound::class.java) {
            namespaceService.listAdmins(namespaceId)
        }

        verify(namespaceAdminEntityRepository, never()).findAllByNamespaceId(any())
    }

    @Test
    fun listAdmins_existsHasAdmins_correct() {
        val namespaceId = 1L
        val creator = uuid1
        val name = "NS1"
        val description = "Some application group"

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )

        `when`(
            namespaceAdminEntityRepository.findAllByNamespaceId(namespaceId),
        ).thenReturn(
            listOf(
                NamespaceAdminEntity(
                    id = 1L,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    namespaceId = namespaceId,
                    assignerSub = uuid0,
                    userSub = uuid1,
                ),
                NamespaceAdminEntity(
                    id = 2L,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    namespaceId = namespaceId,
                    assignerSub = uuid0,
                    userSub = uuid2,
                ),
            ),
        )

        val actual = namespaceService.listAdmins(namespaceId)
        val expected = listOf(uuid1, uuid2)
        AssertionsJ.assertThat(actual).allMatch { expected.contains(it.subject) }
    }

    @Test
    fun addAdmin_notExists_error() {
        val namespaceId = 1L
        val creator = uuid0
        val sub = uuid1

        `when`(
            keycloakService.isUserExists(uuid1),
        ).thenReturn(true)

        `when`(
            namespaceAdminEntityRepository.save(
                argThat { it -> it.namespaceId == namespaceId && it.assignerSub == creator && it.userSub == sub },
            ),
        ).thenThrow(DataIntegrityViolationException::class.java)

        Assertions.assertThrows(ConditionFailureException.NotFound::class.java) {
            namespaceService.addAdmin(creator, namespaceId, sub)
        }
    }

    @Test
    fun addAdmin_existsNoAdmin_true() {
        val namespaceId = 1L
        val adminId = 2L
        val creator = uuid0
        val sub = uuid1

        `when`(
            keycloakService.isUserExists(uuid1),
        ).thenReturn(true)

        `when`(
            namespaceAdminEntityRepository.save(
                argThat { it -> it.namespaceId == namespaceId && it.assignerSub == creator && it.userSub == sub },
            ),
        ).thenReturn(
            NamespaceAdminEntity(
                id = adminId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                namespaceId = namespaceId,
                assignerSub = creator,
                userSub = sub,
            ),
        )

        val actual = namespaceService.addAdmin(creator, namespaceId, sub)
        val expected = true
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun addAdmin_existsHasAdmin_error() {
        val namespaceId = 1L
        val creator = uuid0
        val sub = uuid1

        `when`(
            keycloakService.isUserExists(uuid1),
        ).thenReturn(true)

        `when`(
            namespaceAdminEntityRepository.save(
                argThat { it -> it.namespaceId == namespaceId && it.assignerSub == creator && it.userSub == sub },
            ),
        ).thenThrow(DuplicateKeyException::class.java)

        val actual = namespaceService.addAdmin(creator, namespaceId, sub)
        val expected = false
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun removeAdmin_notExists_error() {
        val namespaceId = 1L
        val sub = uuid1

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(null)

        Assertions.assertThrows(ConditionFailureException.NotFound::class.java) {
            namespaceService.removeAdmin(namespaceId, sub)
        }
    }

    @Test
    fun removeAdmin_existsNoAdmin_false() {
        val namespaceId = 1L
        val creator = uuid0
        val name = "NS1"
        val description = "Some application group"
        val sub = uuid1

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )

        `when`(
            namespaceAdminEntityRepository.findByNamespaceIdAndUserSub(eq(namespaceId), eq(sub), any()),
        ).thenReturn(null)

        val actual = namespaceService.removeAdmin(namespaceId, sub)
        val expected = false
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun removeAdmin_existsHasAdmin_true() {
        val namespaceId = 1L
        val adminId = 2L
        val creator = uuid0
        val name = "NS1"
        val description = "Some application group"
        val sub = uuid1

        `when`(
            namespaceEntityRepository.findById(
                eq(namespaceId),
                any(),
            ),
        ).thenReturn(
            NamespaceEntity(
                id = namespaceId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                creatorSub = creator,
                name = name,
                description = description,
            ),
        )

        `when`(
            namespaceAdminEntityRepository.findByNamespaceIdAndUserSub(eq(namespaceId), eq(sub), any()),
        ).thenReturn(
            NamespaceAdminEntity(
                id = adminId,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                namespaceId = namespaceId,
                assignerSub = creator,
                userSub = sub,
            ),
        )

        `when`(
            namespaceAdminEntityRepository.removeById(adminId),
        ).thenReturn(true)

        val actual = namespaceService.removeAdmin(namespaceId, sub)
        val expected = true
        Assertions.assertEquals(expected, actual)
    }
}
