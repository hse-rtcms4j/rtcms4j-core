package ru.enzhine.rtcms4j.core.service.internal

import org.slf4j.LoggerFactory
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
import ru.enzhine.rtcms4j.core.mapper.toClientCredentials
import ru.enzhine.rtcms4j.core.mapper.toService
import ru.enzhine.rtcms4j.core.repository.db.ApplicationEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.ApplicationManagerEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier
import ru.enzhine.rtcms4j.core.service.external.KeycloakService
import ru.enzhine.rtcms4j.core.service.internal.dto.Application
import ru.enzhine.rtcms4j.core.service.internal.dto.ClientCredentials
import ru.enzhine.rtcms4j.core.service.internal.tx.registerCommitCallback
import java.util.UUID

@Service
class ApplicationServiceImpl(
    private val applicationEntityRepository: ApplicationEntityRepository,
    private val applicationManagerEntityRepository: ApplicationManagerEntityRepository,
    private val defaultPaginationProperties: DefaultPaginationProperties,
    private val namespaceService: NamespaceService,
    private val keycloakService: KeycloakService,
) : ApplicationService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun createApplication(
        creator: UUID,
        namespaceId: Long,
        name: String,
        description: String,
    ): Application {
        val namespace = namespaceService.getNamespaceById(namespaceId, true)

        val applicationEntity =
            try {
                applicationEntityRepository.save(
                    newApplicationEntity(
                        namespaceId = namespace.id,
                        creatorSub = creator,
                        name = name,
                        description = description,
                    ),
                )
            } catch (ex: DuplicateKeyException) {
                throw nameKeyDuplicatedException(ex)
            } catch (_: DataIntegrityViolationException) {
                throw namespaceNotFoundException(namespaceId)
            }

        registerCommitCallback {
            try {
                val clientId = keycloakService.buildClientId(namespaceId, applicationEntity.id)
                val res = keycloakService.createNewApplicationClient(clientId)
                println(res)
            } catch (ex: Throwable) {
                logger.error("Unable to create client for application with id ${applicationEntity.id}", ex)
            }
        }

        return applicationEntity.toService()
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
        val namespace = namespaceService.getNamespaceById(namespaceId, false)

        val pageable =
            pageable
                ?: PageRequest.of(0, defaultPaginationProperties.pageSize)

        return applicationEntityRepository
            .findAllByNamespaceIdAndName(namespace.id, name, pageable)
            .map { it.toService() }
    }

    override fun getApplicationClientCredentials(
        namespaceId: Long,
        applicationId: Long,
    ): ClientCredentials {
        val namespace = namespaceService.getNamespaceById(namespaceId, false)

        val applicationEntity =
            applicationEntityRepository
                .findById(applicationId)
                ?: throw applicationNotFoundException(applicationId)

        if (applicationEntity.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        val clientId = keycloakService.buildClientId(namespaceId, applicationEntity.id)
        return keycloakService
            .findApplicationClient(clientId)
            .toClientCredentials()
    }

    override fun rotateApplicationClientCredentials(
        namespaceId: Long,
        applicationId: Long,
    ): ClientCredentials {
        val namespace = namespaceService.getNamespaceById(namespaceId, false)

        val applicationEntity =
            applicationEntityRepository
                .findById(applicationId)
                ?: throw applicationNotFoundException(applicationId)

        if (applicationEntity.namespaceId != namespace.id) {
            throw applicationNotFoundException(applicationId)
        }

        val clientId = keycloakService.buildClientId(namespaceId, applicationEntity.id)
        return keycloakService
            .rotateApplicationClientPassword(clientId)
            .toClientCredentials()
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
                applicationId = applicationId,
                userSub = sub,
                modifier = QueryModifier.FOR_UPDATE,
            )
                ?: return false

        return applicationManagerEntityRepository.removeById(manager.id)
    }
}
