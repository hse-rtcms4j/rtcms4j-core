package ru.enzhine.rtcms4j.core.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.enzhine.rtcms4j.core.builder.nameKeyDuplicatedException
import ru.enzhine.rtcms4j.core.builder.namespaceNotFoundException
import ru.enzhine.rtcms4j.core.builder.newNamespaceAdminEntity
import ru.enzhine.rtcms4j.core.builder.newNamespaceEntity
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.NamespaceAdminEntityRepository
import ru.enzhine.rtcms4j.core.repository.NamespaceEntityRepository
import ru.enzhine.rtcms4j.core.repository.util.QueryModifier
import ru.enzhine.rtcms4j.core.service.dto.Namespace
import java.util.UUID

@Service
class NamespaceServiceImpl(
    private val namespaceEntityRepository: NamespaceEntityRepository,
    private val namespaceAdminEntityRepository: NamespaceAdminEntityRepository,
    private val defaultPaginationProperties: DefaultPaginationProperties,
) : NamespaceService {
    override fun createNamespace(
        creator: UUID,
        name: String,
        description: String,
    ): Namespace {
        try {
            val namespaceEntity =
                namespaceEntityRepository.save(
                    newNamespaceEntity(
                        creatorSub = creator,
                        name = name,
                        description = description,
                    ),
                )

            return namespaceEntity.toService()
        } catch (ex: DuplicateKeyException) {
            throw nameKeyDuplicatedException(ex)
        }
    }

    override fun getNamespaceById(namespaceId: Long): Namespace =
        namespaceEntityRepository.findById(namespaceId)?.toService()
            ?: throw namespaceNotFoundException(namespaceId)

    @Transactional
    override fun updateNamespace(
        namespaceId: Long,
        name: String?,
        description: String?,
    ): Namespace {
        val namespaceEntity =
            namespaceEntityRepository.findById(namespaceId, QueryModifier.FOR_UPDATE)
                ?: throw namespaceNotFoundException(namespaceId)

        if (name == null && description == null) {
            return namespaceEntity.toService()
        }

        name?.let { namespaceEntity.name = it }
        description?.let { namespaceEntity.description = it }

        try {
            val updatedNamespaceEntity =
                namespaceEntityRepository.update(namespaceEntity)

            return updatedNamespaceEntity.toService()
        } catch (ex: DuplicateKeyException) {
            throw nameKeyDuplicatedException(ex)
        }
    }

    override fun findNamespaces(
        name: String?,
        pageable: Pageable?,
    ): Page<Namespace> {
        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return namespaceEntityRepository.findAllByName(name, pageable).map { it.toService() }
    }

    override fun deleteNamespace(namespaceId: Long): Boolean = namespaceEntityRepository.removeById(namespaceId)

    override fun listAdmins(id: Long): List<UUID> = namespaceAdminEntityRepository.findAllByNamespaceId(id).map { it.userSub }

    override fun addAdmin(
        assigner: UUID,
        namespaceId: Long,
        sub: UUID,
    ): Boolean {
        try {
            namespaceAdminEntityRepository.save(
                newNamespaceAdminEntity(
                    namespaceId = namespaceId,
                    assignerSub = assigner,
                    userSub = sub,
                ),
            )
            return true
        } catch (_: DuplicateKeyException) {
            return false
        } catch (_: DataIntegrityViolationException) {
            throw namespaceNotFoundException(namespaceId)
        }
    }

    @Transactional
    override fun removeAdmin(
        namespaceId: Long,
        sub: UUID,
    ): Boolean {
        namespaceEntityRepository.findById(namespaceId)
            ?: throw namespaceNotFoundException(namespaceId)

        val admin =
            namespaceAdminEntityRepository.findByNamespaceIdAndUserSub(namespaceId, sub, QueryModifier.FOR_UPDATE)
                ?: return false

        return namespaceAdminEntityRepository.removeById(admin.id)
    }
}
