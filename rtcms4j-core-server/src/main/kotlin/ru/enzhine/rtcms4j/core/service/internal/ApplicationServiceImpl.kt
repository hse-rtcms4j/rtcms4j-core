package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.enzhine.rtcms4j.core.builder.applicationNotFoundException
import ru.enzhine.rtcms4j.core.builder.nameKeyDuplicatedException
import ru.enzhine.rtcms4j.core.builder.namespaceNotFoundException
import ru.enzhine.rtcms4j.core.builder.newApplicationEntity
import ru.enzhine.rtcms4j.core.builder.newApplicationManagerEntity
import ru.enzhine.rtcms4j.core.config.props.DefaultPaginationProperties
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.ApplicationManagerEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier
import ru.enzhine.rtcms4j.core.service.external.KeycloakService
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import java.util.UUID

@Service
class ApplicationServiceImpl(
    private val applicationEntityRepository: ApplicationEntityRepository,
    private val applicationManagerEntityRepository: ApplicationManagerEntityRepository,
    private val defaultPaginationProperties: DefaultPaginationProperties,
    private val namespaceService: NamespaceService,
    private val keycloakService: KeycloakService,
) : ApplicationService {
    @Transactional
    override fun createApplication(
        creator: UUID,
        namespaceId: Long,
        name: String,
        description: String,
    ): Application {
        val namespace = namespaceService.getNamespaceById(namespaceId, true)

        try {
            val applicationEntity =
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = creator,
                        name = name,
                        description = description,
                    ),
                )

            val clientId = keycloakService.buildClientId(namespaceId, applicationEntity.id)
            keycloakService.createNewApplicationClient(clientId)

            return applicationEntity.toService()
        } catch (ex: DuplicateKeyException) {
            throw nameKeyDuplicatedException(ex)
        } catch (_: DataIntegrityViolationException) {
            throw namespaceNotFoundException(namespaceId)
        }
    }

    override fun getApplicationById(
        namespaceId: Long,
        applicationId: Long,
        forShare: Boolean,
    ): Application {
        val namespace = namespaceService.getNamespaceById(namespaceId, forShare)

        val application =
            applicationEntityRepository
                .findById(
                    id = applicationId,
                    modifier = if (forShare) QueryModifier.FOR_SHARE else QueryModifier.NONE,
                )?.toService()
                ?: throw applicationNotFoundException(applicationId)

        if (application.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        return application
    }

    @Transactional
    override fun updateApplication(
        namespaceId: Long,
        applicationId: Long,
        name: String?,
        description: String?,
    ): Application {
        val namespace = namespaceService.getNamespaceById(namespaceId, true)

        val applicationEntity =
            applicationEntityRepository.findById(applicationId, QueryModifier.FOR_UPDATE)
                ?: throw applicationNotFoundException(applicationId)

        if (applicationEntity.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        if (name == null && description == null) {
            return applicationEntity.toService()
        }

        name?.let { applicationEntity.name = name }
        description?.let { applicationEntity.description = description }

        try {
            val updatedApplicationEntity =
                applicationEntityRepository.update(applicationEntity)

            return updatedApplicationEntity.toService()
        } catch (ex: DuplicateKeyException) {
            throw nameKeyDuplicatedException(ex)
        }
    }

    override fun findApplications(
        namespaceId: Long,
        name: String?,
        pageable: Pageable?,
    ): Page<Application> {
        namespaceService.getNamespaceById(namespaceId, false)

        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return applicationEntityRepository.findAllByName(namespaceId, name, pageable).map { it.toService() }
    }

    override fun deleteApplication(
        namespaceId: Long,
        applicationId: Long,
    ): Boolean {
        val namespace = namespaceService.getNamespaceById(namespaceId, false)

        val applicationEntity =
            applicationEntityRepository.findById(applicationId, QueryModifier.FOR_UPDATE)
                ?: throw applicationNotFoundException(applicationId)

        if (applicationEntity.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        return applicationEntityRepository.removeById(applicationId)
    }

    override fun listManagers(
        namespaceId: Long,
        applicationId: Long,
    ): List<UUID> {
        val namespace = namespaceService.getNamespaceById(namespaceId, false)

        val applicationEntity =
            applicationEntityRepository.findById(applicationId, QueryModifier.FOR_SHARE)
                ?: throw applicationNotFoundException(applicationId)

        if (applicationEntity.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        return applicationManagerEntityRepository.findAllByApplicationId(applicationId).map { it.userSub }
    }

    @Transactional
    override fun addManager(
        assigner: UUID,
        namespaceId: Long,
        applicationId: Long,
        sub: UUID,
    ): Boolean {
        val namespace = namespaceService.getNamespaceById(namespaceId, true)

        val applicationEntity =
            applicationEntityRepository.findById(applicationId, QueryModifier.FOR_SHARE)
                ?: throw applicationNotFoundException(applicationId)

        if (applicationEntity.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        try {
            applicationManagerEntityRepository.save(
                newApplicationManagerEntity(
                    applicationId = applicationId,
                    assignerSub = assigner,
                    userSub = sub,
                ),
            )
            return true
        } catch (_: DuplicateKeyException) {
            return false
        } catch (_: DataIntegrityViolationException) {
            throw applicationNotFoundException(applicationId)
        }
    }

    @Transactional
    override fun removeManager(
        namespaceId: Long,
        applicationId: Long,
        sub: UUID,
    ): Boolean {
        val namespace = namespaceService.getNamespaceById(namespaceId, true)

        val applicationEntity =
            applicationEntityRepository.findById(applicationId, QueryModifier.FOR_SHARE)
                ?: throw applicationNotFoundException(applicationId)

        if (applicationEntity.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        val manager =
            applicationManagerEntityRepository.findByApplicationIdAndUserSub(
                applicationId,
                sub,
                QueryModifier.FOR_UPDATE,
            )
                ?: return false

        return applicationManagerEntityRepository.removeById(manager.id)
    }
}
