package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationManagerEntity
import java.util.UUID
import kotlin.jvm.Throws

interface ApplicationManagerEntityRepository {
    /**
     * @throws DuplicateKeyException user already assigned as application manager
     * @throws DataIntegrityViolationException application does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(applicationManagerEntity: ApplicationManagerEntity): ApplicationManagerEntity

    fun findAllByApplicationId(applicationId: Long): List<ApplicationManagerEntity>

    fun findByApplicationIdAndUserSub(
        applicationId: Long,
        userSub: UUID,
    ): ApplicationManagerEntity?

    fun findById(id: Long): ApplicationManagerEntity?

    fun removeById(id: Long): Boolean
}
