package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.db.dto.NamespaceEntity
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class NamespaceEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : NamespaceEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<NamespaceEntity> { rs, _ ->
                NamespaceEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
                    creatorSub = rs.getObject("creator_sub", UUID::class.java),
                    name = rs.getString("name"),
                    description = rs.getString("description"),
                )
            }
    }

    override fun findAllByName(
        name: String?,
        pageable: Pageable,
    ): Page<NamespaceEntity> {
        val total = countAllNamespacesByName(name, pageable)
        var content = emptyList<NamespaceEntity>()
        if (total != 0L) {
            content = findAllNamespacesByName(name, pageable)
        }

        return PageImpl(content, pageable, total)
    }

    private fun findAllNamespacesByName(
        name: String?,
        pageable: Pageable,
    ): List<NamespaceEntity> =
        npJdbc.query(
            """
            select * from namespace
            where (:name::text is null) or
                  (name ilike '%' || :name::text || '%')
            order by name
            offset :offset limit :limit;
            """.trimIndent(),
            mapOf(
                "name" to name,
                "offset" to pageable.offset,
                "limit" to pageable.pageSize,
            ),
            ROW_MAPPER,
        )

    private fun countAllNamespacesByName(
        name: String?,
        pageable: Pageable,
    ): Long =
        npJdbc.queryForObject(
            """
            select count(*) from namespace
            where (:name::text is null) or
                  (name ilike '%' || :name::text || '%');
            """.trimIndent(),
            mapOf(
                "name" to name,
                "offset" to pageable.offset,
                "limit" to pageable.pageSize,
            ),
            Long::class.java,
        ) ?: 0L

    override fun findById(
        id: Long,
        modifier: QueryModifier,
    ): NamespaceEntity? =
        npJdbc
            .query(
                """
                select * from namespace
                where id = :id
                ${modifier.suffix};
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
                ROW_MAPPER,
            ).firstOrNull()

    override fun save(namespaceEntity: NamespaceEntity): NamespaceEntity =
        npJdbc
            .query(
                """
                insert into namespace (creator_sub, name, description)
                values (:creator_sub, :name, :description)
                returning *;
                """.trimIndent(),
                mapOf(
                    "creator_sub" to namespaceEntity.creatorSub,
                    "name" to namespaceEntity.name,
                    "description" to namespaceEntity.description,
                ),
                ROW_MAPPER,
            ).first()

    override fun update(namespaceEntity: NamespaceEntity): NamespaceEntity =
        npJdbc
            .query(
                """
                update namespace
                set updated_at = now(),
                    name = :name,
                    description = :description
                where id = :id
                returning *;
                """.trimIndent(),
                mapOf(
                    "name" to namespaceEntity.name,
                    "description" to namespaceEntity.description,
                    "id" to namespaceEntity.id,
                ),
                ROW_MAPPER,
            ).first()

    override fun removeById(id: Long): Boolean =
        npJdbc.update(
            """
            delete from namespace
            where id = :id;
            """.trimIndent(),
            mapOf(
                "id" to id,
            ),
        ) != 0
}
