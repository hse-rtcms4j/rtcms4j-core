package ru.enzhine.rtcms4j.core.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.service.dto.Namespace
import ru.enzhine.rtcms4j.core.service.exception.ConditionFailureException
import java.util.UUID

interface NamespaceService {
    @Throws(ConditionFailureException.KeyDuplicated::class)
    fun createNamespace(
        creator: UUID,
        name: String,
        description: String,
    ): Namespace

    fun getNamespaceById(id: Long): Namespace?

    @Throws(ConditionFailureException.KeyDuplicated::class, ConditionFailureException.NotFound::class)
    fun updateNamespace(
        id: Long,
        name: String?,
        description: String?,
    ): Namespace

    fun findNamespaces(
        name: String?,
        pageable: Pageable?,
    ): Page<Namespace>

    @Throws(ConditionFailureException.NotFound::class)
    fun deleteNamespace(id: Long): Boolean

    fun listAdmins(id: Long): List<UUID>

    @Throws(ConditionFailureException.NotFound::class)
    fun addAdmin(
        assigner: UUID,
        id: Long,
        sub: UUID,
    ): Boolean

    @Throws(ConditionFailureException.NotFound::class)
    fun removeAdmin(
        id: Long,
        sub: UUID,
    ): Boolean
}
