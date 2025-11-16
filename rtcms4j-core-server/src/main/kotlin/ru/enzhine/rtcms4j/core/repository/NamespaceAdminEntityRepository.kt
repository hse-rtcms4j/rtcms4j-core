package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceAdminEntity
import ru.enzhine.rtcms4j.core.repository.util.QuerySuffixes
import java.util.UUID

interface NamespaceAdminEntityRepository {
    /**
     * @throws DuplicateKeyException user already assigned as namespace admin
     * @throws DataIntegrityViolationException namespace does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(namespaceAdminEntity: NamespaceAdminEntity): NamespaceAdminEntity

    fun findAllByNamespaceId(namespaceId: Long): List<NamespaceAdminEntity>

    fun findByNamespaceIdAndUserSub(
        namespaceId: Long,
        userSub: UUID,
        querySuffix: String = QuerySuffixes.None,
    ): NamespaceAdminEntity?

    fun findById(id: Long): NamespaceAdminEntity?

    fun removeById(id: Long): Boolean
}
