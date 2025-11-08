package ru.enzhine.rtcms4j.core.repository

import ru.enzhine.rtcms4j.core.repository.dto.ApplicationManagerEntity
import java.util.UUID

interface ApplicationManagerEntityRepository {
    fun save(applicationManagerEntity: ApplicationManagerEntity): ApplicationManagerEntity

    fun findAllByApplicationId(applicationId: Long): List<ApplicationManagerEntity>

    fun findByApplicationIdAndUserSub(
        applicationId: Long,
        userSub: UUID,
    ): ApplicationManagerEntity?

    fun findById(id: Long): ApplicationManagerEntity?

    fun removeById(id: Long): Boolean
}
