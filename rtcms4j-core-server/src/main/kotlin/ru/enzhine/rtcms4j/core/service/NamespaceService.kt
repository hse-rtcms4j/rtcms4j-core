package ru.enzhine.rtcms4j.core.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.service.dto.Namespace
import java.util.UUID

interface NamespaceService {
    fun createNamespace(creator: UUID, name: String, description: String): Namespace

    fun getNamespaceById(id: Long): Namespace

    fun updateNamespace(namespace: Namespace): Namespace

    fun findNamespaces(name: String?, pageable: Pageable?): Page<Namespace>

    fun deleteNamespace(namespace: Namespace): Boolean

    fun listAdmins(namespace: Namespace): List<UUID>

    fun addAdmin(namespace: Namespace, sub: UUID): Boolean

    fun removeAdmin(namespace: Namespace, sub: UUID): Boolean
}
