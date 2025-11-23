package ru.enzhine.rtcms4j.core.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationManagerEntity
import ru.enzhine.rtcms4j.core.repository.util.QueryModifier
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class ApplicationManagerEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ApplicationManagerEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ApplicationManagerEntity> { rs, _ ->
                ApplicationManagerEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
                    applicationId = rs.getLong("application_id"),
                    assignerSub = rs.getObject("assigner_sub", UUID::class.java),
                    userSub = rs.getObject("user_sub", UUID::class.java),
                )
            }
    }

    override fun save(applicationManagerEntity: ApplicationManagerEntity): ApplicationManagerEntity =
        npJdbc
            .query(
                """
                insert into application_manager (application_id, assigner_sub, user_sub)
                values (:application_id, :assigner_sub, :user_sub)
                returning *;
                """.trimIndent(),
                mapOf(
                    "application_id" to applicationManagerEntity.applicationId,
                    "assigner_sub" to applicationManagerEntity.assignerSub,
                    "user_sub" to applicationManagerEntity.userSub,
                ),
                ROW_MAPPER,
            ).first()

    override fun findAllByApplicationId(applicationId: Long): List<ApplicationManagerEntity> =
        npJdbc
            .query(
                """
                select * from application_manager
                where application_id = :application_id;
                """.trimIndent(),
                mapOf(
                    "application_id" to applicationId,
                ),
                ROW_MAPPER,
            )

    override fun findByApplicationIdAndUserSub(
        applicationId: Long,
        userSub: UUID,
        modifier: QueryModifier,
    ): ApplicationManagerEntity? =
        npJdbc
            .query(
                """
                select * from application_manager
                where application_id = :application_id and
                      user_sub = :user_sub
                ${modifier.suffix};
                """.trimIndent(),
                mapOf(
                    "application_id" to applicationId,
                    "user_sub" to userSub,
                ),
                ROW_MAPPER,
            ).firstOrNull()

    override fun findById(id: Long): ApplicationManagerEntity? =
        npJdbc
            .query(
                """
                select * from application_manager
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
                delete from application_manager
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
