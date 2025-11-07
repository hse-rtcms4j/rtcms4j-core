package ru.enzhine.rtcms4j.core.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceEntity

interface ApplicationEntityRepository {
    fun findAllByName(
        namespaceId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ApplicationEntity>

    fun findById(
        namespaceId: Long,
        id: Long
    ): ApplicationEntity?

    fun save(
        namespaceEntity: ApplicationEntity
    ): ApplicationEntity

    fun update(
        namespaceEntity: NamespaceEntity
    ): NamespaceEntity

    fun removeById(
        namespaceId: Long,
        id: Long
    ): Boolean
}
