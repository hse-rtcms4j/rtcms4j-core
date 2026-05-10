package ru.enzhine.rtcms4j.core.repository.db

import ru.enzhine.rtcms4j.core.repository.db.dto.OutboxTaskKcClientEntity

interface OutboxTaskKcClientEntityRepository {
    fun save(outboxTaskKcClientEntity: OutboxTaskKcClientEntity): OutboxTaskKcClientEntity

    fun updateAttemptAndSkipProperties(outboxTaskKcClientEntity: OutboxTaskKcClientEntity): OutboxTaskKcClientEntity

    fun removeById(id: Long): Boolean

    fun findBatchForUpdateSkipLocked(
        batchSize: Int,
        maxAttempt: Int,
    ): List<OutboxTaskKcClientEntity>
}
