package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier

interface NamespaceEntityRepository {
    fun findAllByName(
        name: String?,
        pageable: Pageable,
    ): Page<NamespaceEntity>

    fun findById(
        id: Long,
        modifier: QueryModifier = QueryModifier.NONE,
    ): NamespaceEntity?

    /**
     * @throws DuplicateKeyException name duplication
     */
    @Throws(DuplicateKeyException::class)
    fun save(namespaceEntity: NamespaceEntity): NamespaceEntity

    /**
     * @throws DuplicateKeyException name duplication
     */
    @Throws(DuplicateKeyException::class)
    fun update(namespaceEntity: NamespaceEntity): NamespaceEntity

    fun removeById(id: Long): Boolean
}
