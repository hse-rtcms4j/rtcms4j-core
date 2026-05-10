package ru.enzhine.rtcms4j.core.repository.db.dto

import java.time.OffsetDateTime

data class OutboxTaskKcClientEntity(
    val id: Long,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    var attempt: Int,
    var skip: Boolean,
    var action: Action,
    var namespaceId: Long,
    var applicationId: Long,
) {
    enum class Action {
        CREATE,
        DELETE,
    }
}
