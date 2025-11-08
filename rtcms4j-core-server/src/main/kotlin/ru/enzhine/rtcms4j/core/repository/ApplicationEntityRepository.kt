package ru.enzhine.rtcms4j.core.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationEntity

interface ApplicationEntityRepository {
    fun findAllByName(
        namespaceId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ApplicationEntity>

    fun findById(id: Long): ApplicationEntity?

    fun save(namespaceEntity: ApplicationEntity): ApplicationEntity

    fun update(namespaceEntity: ApplicationEntity): ApplicationEntity

    fun removeById(id: Long): Boolean
}
