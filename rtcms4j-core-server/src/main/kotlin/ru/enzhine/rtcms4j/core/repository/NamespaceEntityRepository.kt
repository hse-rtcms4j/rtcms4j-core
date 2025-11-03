package ru.enzhine.rtcms4j.core.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceEntity

interface NamespaceEntityRepository {
    fun findAllByName(
        name: String?,
        pageable: Pageable,
    ): Page<NamespaceEntity>

    fun findById(id: Long): NamespaceEntity?

    fun save(namespaceEntity: NamespaceEntity): NamespaceEntity

    fun update(namespaceEntity: NamespaceEntity): NamespaceEntity

    fun removeById(id: Long): Boolean
}
