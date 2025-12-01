package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.service.internal.dto.Namespace
import java.util.UUID

interface NamespaceService {
    @Throws(ConditionFailureException.KeyDuplicated::class)
    fun createNamespace(
        creator: UUID,
        name: String,
        description: String,
    ): Namespace

    @Throws(ConditionFailureException.NotFound::class)
    fun getNamespaceById(
        namespaceId: Long,
        forShare: Boolean,
    ): Namespace

    @Throws(ConditionFailureException.KeyDuplicated::class, ConditionFailureException.NotFound::class)
    fun updateNamespace(
        namespaceId: Long,
        name: String?,
        description: String?,
    ): Namespace

    fun findNamespaces(
        name: String?,
        pageable: Pageable?,
    ): Page<Namespace>

    fun deleteNamespace(namespaceId: Long): Boolean

    @Throws(ConditionFailureException.NotFound::class)
    fun listAdmins(namespaceId: Long): List<UUID>

    @Throws(ConditionFailureException.NotFound::class)
    fun addAdmin(
        assigner: UUID,
        namespaceId: Long,
        sub: UUID,
    ): Boolean

    @Throws(ConditionFailureException.NotFound::class)
    fun removeAdmin(
        namespaceId: Long,
        sub: UUID,
    ): Boolean
}
