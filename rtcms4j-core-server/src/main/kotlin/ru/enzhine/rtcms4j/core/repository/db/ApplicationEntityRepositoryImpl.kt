package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class ApplicationEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ApplicationEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ApplicationEntity> { rs, _ ->
                ApplicationEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
                    namespaceId = rs.getLong("namespace_id"),
                    creatorSub = rs.getObject("creator_sub", UUID::class.java),
                    name = rs.getString("name"),
                    description = rs.getString("description"),
                    creationByService = rs.getBoolean("creation_by_service"),
                )
            }
    }

    override fun findAllByUserSub(userSub: UUID): List<ApplicationEntity> =
        npJdbc.query(
            """
            select a.* from application a
            inner join application_manager am
            on a.id = am.application_id
            where am.user_sub = :user_sub;
            """.trimIndent(),
            mapOf(
                "user_sub" to userSub,
            ),
            ROW_MAPPER,
        )

    override fun findAllByNamespaceIdAndName(
        namespaceId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ApplicationEntity> {
        val total = countAllApplicationsByName(namespaceId, name, pageable)
        var content = emptyList<ApplicationEntity>()
        if (total != 0L) {
            content = findAllApplicationsByName(namespaceId, name, pageable)
        }

        return PageImpl(content, pageable, total)
    }

    private fun findAllApplicationsByName(
        namespaceId: Long,
        name: String?,
        pageable: Pageable,
    ): List<ApplicationEntity> =
        npJdbc.query(
            """
            select * from application
            where namespace_id = :namespace_id and
                  ((:name::text is null) or
                  (name ilike '%' || :name::text || '%'))
            order by name
            offset :offset limit :limit;
            """.trimIndent(),
            mapOf(
                "namespace_id" to namespaceId,
                "name" to name,
                "offset" to pageable.offset,
                "limit" to pageable.pageSize,
            ),
            ROW_MAPPER,
        )

    private fun countAllApplicationsByName(
        namespaceId: Long,
        name: String?,
        pageable: Pageable,
    ): Long =
        npJdbc.queryForObject(
            """
            select count(*) from application
            where namespace_id = :namespace_id and
                  ((:name::text is null) or
                  (name ilike '%' || :name::text || '%'));
            """.trimIndent(),
            mapOf(
                "namespace_id" to namespaceId,
                "name" to name,
                "offset" to pageable.offset,
                "limit" to pageable.pageSize,
            ),
            Long::class.java,
        ) ?: 0L

    override fun findById(
        id: Long,
        modifier: QueryModifier,
    ): ApplicationEntity? =
        npJdbc
            .query(
                """
                select * from application
                where id = :id
                ${modifier.suffix};
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
                ROW_MAPPER,
            ).firstOrNull()

    override fun save(applicationEntity: ApplicationEntity): ApplicationEntity =
        npJdbc
            .query(
                """
                insert into application (namespace_id, creator_sub, name, description, creation_by_service)
                values (:namespace_id, :creator_sub, :name, :description, :creation_by_service)
                returning *;
                """.trimIndent(),
                mapOf(
                    "namespace_id" to applicationEntity.namespaceId,
                    "creator_sub" to applicationEntity.creatorSub,
                    "name" to applicationEntity.name,
                    "description" to applicationEntity.description,
                    "creation_by_service" to applicationEntity.creationByService,
                ),
                ROW_MAPPER,
            ).first()

    override fun update(applicationEntity: ApplicationEntity): ApplicationEntity =
        npJdbc
            .query(
                """
                update application
                set updated_at = now(),
                    name = :name,
                    description = :description,
                    creation_by_service = :creation_by_service
                where id = :id
                returning *;
                """.trimIndent(),
                mapOf(
                    "name" to applicationEntity.name,
                    "description" to applicationEntity.description,
                    "creation_by_service" to applicationEntity.creationByService,
                    "id" to applicationEntity.id,
                ),
                ROW_MAPPER,
            ).first()

    override fun removeById(id: Long): Boolean =
        npJdbc.update(
            """
            delete from application
            where id = :id;
            """.trimIndent(),
            mapOf(
                "id" to id,
            ),
        ) != 0
}
