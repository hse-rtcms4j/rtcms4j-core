package ru.enzhine.rtcms4j.core.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.NamespaceAdminEntity
import ru.enzhine.rtcms4j.core.repository.util.QueryModifier
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class NamespaceAdminEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : NamespaceAdminEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<NamespaceAdminEntity> { rs, _ ->
                NamespaceAdminEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
                    namespaceId = rs.getLong("namespace_id"),
                    assignerSub = rs.getObject("assigner_sub", UUID::class.java),
                    userSub = rs.getObject("user_sub", UUID::class.java),
                )
            }
    }

    override fun save(namespaceAdminEntity: NamespaceAdminEntity): NamespaceAdminEntity =
        npJdbc
            .query(
                """
                insert into namespace_admin (namespace_id, assigner_sub, user_sub)
                values (:namespace_id, :assigner_sub, :user_sub)
                returning *;
                """.trimIndent(),
                mapOf(
                    "namespace_id" to namespaceAdminEntity.namespaceId,
                    "assigner_sub" to namespaceAdminEntity.assignerSub,
                    "user_sub" to namespaceAdminEntity.userSub,
                ),
                ROW_MAPPER,
            ).first()

    override fun findAllByNamespaceId(namespaceId: Long): List<NamespaceAdminEntity> =
        npJdbc
            .query(
                """
                select * from namespace_admin
                where namespace_id = :namespace_id;
                """.trimIndent(),
                mapOf(
                    "namespace_id" to namespaceId,
                ),
                ROW_MAPPER,
            )

    override fun findByNamespaceIdAndUserSub(
        namespaceId: Long,
        userSub: UUID,
        modifier: QueryModifier,
    ): NamespaceAdminEntity? =
        npJdbc
            .query(
                """
                select * from namespace_admin
                where namespace_id = :namespace_id and
                      user_sub = :user_sub
                ${modifier.suffix};
                """.trimIndent(),
                mapOf(
                    "namespace_id" to namespaceId,
                    "user_sub" to userSub,
                ),
                ROW_MAPPER,
            ).firstOrNull()

    override fun findById(id: Long): NamespaceAdminEntity? =
        npJdbc
            .query(
                """
                select * from namespace_admin
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
                ROW_MAPPER,
            ).firstOrNull()

    override fun removeById(id: Long): Boolean =
        npJdbc
            .update(
                """
                delete from namespace_admin
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
