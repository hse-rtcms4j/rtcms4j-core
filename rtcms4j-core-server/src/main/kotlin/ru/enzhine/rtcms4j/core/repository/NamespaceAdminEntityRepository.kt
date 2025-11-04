package ru.enzhine.rtcms4j.core.repository

import ru.enzhine.rtcms4j.core.repository.dto.NamespaceAdminEntity
import java.util.UUID

interface NamespaceAdminEntityRepository {
    fun save(namespaceAdminEntity: NamespaceAdminEntity): NamespaceAdminEntity

    fun findAllByNamespaceId(namespaceId: Long): List<NamespaceAdminEntity>

    fun findByNamespaceIdAndUserSub(
        namespaceId: Long,
        userSub: UUID,
    ): NamespaceAdminEntity?

    fun findById(id: Long): NamespaceAdminEntity?

    fun removeById(id: Long): Boolean
}
