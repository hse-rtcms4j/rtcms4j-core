package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.db.dto.OutboxTaskKcClientEntity
import java.time.OffsetDateTime

@Repository
class OutboxTaskKcClientEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : OutboxTaskKcClientEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<OutboxTaskKcClientEntity> { rs, _ ->
                OutboxTaskKcClientEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
                    attempt = rs.getInt("attempt"),
                    skip = rs.getBoolean("skip"),
                    action = OutboxTaskKcClientEntity.Action.valueOf(rs.getString("action")),
                    namespaceId = rs.getLong("namespace_id"),
                    applicationId = rs.getLong("application_id"),
                )
            }
    }

    override fun save(outboxTaskKcClientEntity: OutboxTaskKcClientEntity): OutboxTaskKcClientEntity =
        npJdbc
            .query(
                """
                insert into outbox_task_kc_client (attempt, skip, action, namespace_id, application_id)
                values (:attempt, :skip, :action, :namespace_id, :application_id)
                returning *;
                """.trimIndent(),
                mapOf(
                    "attempt" to outboxTaskKcClientEntity.attempt,
                    "skip" to outboxTaskKcClientEntity.skip,
                    "action" to outboxTaskKcClientEntity.action.toString(),
                    "namespace_id" to outboxTaskKcClientEntity.namespaceId,
                    "application_id" to outboxTaskKcClientEntity.applicationId,
                ),
                ROW_MAPPER,
            ).first()

    override fun updateAttemptAndSkipProperties(outboxTaskKcClientEntity: OutboxTaskKcClientEntity): OutboxTaskKcClientEntity =
        npJdbc
            .query(
                """
                update outbox_task_kc_client
                set updated_at = now(),
                    attempt = :attempt,
                    skip = :skip
                where id = :id
                returning *;
                """.trimIndent(),
                mapOf(
                    "attempt" to outboxTaskKcClientEntity.attempt,
                    "skip" to outboxTaskKcClientEntity.skip,
                    "id" to outboxTaskKcClientEntity.id,
                ),
                ROW_MAPPER,
            ).first()

    override fun removeById(id: Long): Boolean =
        npJdbc
            .update(
                """
                delete from outbox_task_kc_client
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0

    override fun findBatchForUpdateSkipLocked(
        batchSize: Int,
        maxAttempt: Int,
    ): List<OutboxTaskKcClientEntity> =
        npJdbc
            .query(
                """
                select id, created_at, updated_at, attempt, skip, action, namespace_id, application_id
                from outbox_task_kc_client
                where skip = false
                order by created_at asc
                limit :batch_size
                for update;
                """.trimIndent(),
                mapOf(
                    "batch_size" to batchSize,
                ),
                ROW_MAPPER,
            )
}
