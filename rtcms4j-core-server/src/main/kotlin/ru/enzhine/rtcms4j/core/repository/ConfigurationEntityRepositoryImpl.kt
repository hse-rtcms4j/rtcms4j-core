package ru.enzhine.rtcms4j.core.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class ConfigurationEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigurationEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ConfigurationEntity> { rs, _ ->
                ConfigurationEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
                    applicationId = rs.getLong("application_id"),
                    creatorSub = rs.getObject("creator_sub", UUID::class.java),
                    name = rs.getString("name"),
                    commitHash = rs.getString("commit_hash"),
                    streamKey = rs.getString("stream_key"),
                    schemaSourceType = SourceType.valueOf(rs.getString("schema_source_type")),
                )
            }
    }

    override fun save(configurationEntity: ConfigurationEntity): ConfigurationEntity =
        npJdbc
            .query(
                """
                insert into configuration (application_id, creator_sub, name, commit_hash, stream_key, schema_source_type)
                values (:application_id, :creator_sub, :name, :commit_hash, :stream_key, :schema_source_type)
                returning *;
                """.trimIndent(),
                mapOf(
                    "application_id" to configurationEntity.applicationId,
                    "creator_sub" to configurationEntity.creatorSub,
                    "name" to configurationEntity.name,
                    "commit_hash" to configurationEntity.commitHash,
                    "stream_key" to configurationEntity.streamKey,
                    "schema_source_type" to configurationEntity.schemaSourceType.toString(),
                ),
                ROW_MAPPER,
            ).first()

    override fun findAllByApplicationIdAndName(
        applicationId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ConfigurationEntity> {
        val total = countAllConfigurationsByApplicationIdAndName(applicationId, name, pageable)
        var content = emptyList<ConfigurationEntity>()
        if (total != 0L) {
            content = findAllConfigurationsByApplicationIdAndName(applicationId, name, pageable)
        }

        return PageImpl(content, pageable, total)
    }

    private fun findAllConfigurationsByApplicationIdAndName(
        applicationId: Long,
        name: String?,
        pageable: Pageable,
    ): List<ConfigurationEntity> =
        npJdbc.query(
            """
            select * from configuration
            where application_id = :application_id and
                  ((:name::text is null) or
                  (name ilike '%' || :name::text || '%'))
            order by name
            offset :offset limit :limit;
            """.trimIndent(),
            mapOf(
                "application_id" to applicationId,
                "name" to name,
                "offset" to pageable.offset,
                "limit" to pageable.pageSize,
            ),
            ROW_MAPPER,
        )

    private fun countAllConfigurationsByApplicationIdAndName(
        applicationId: Long,
        name: String?,
        pageable: Pageable,
    ): Long =
        npJdbc.queryForObject(
            """
            select count(*) from configuration
            where application_id = :application_id and
                  ((:name::text is null) or
                  (name ilike '%' || :name::text || '%'));
            """.trimIndent(),
            mapOf(
                "application_id" to applicationId,
                "name" to name,
                "offset" to pageable.offset,
                "limit" to pageable.pageSize,
            ),
            Long::class.java,
        ) ?: 0L

    override fun findById(id: Long): ConfigurationEntity? =
        npJdbc
            .query(
                """
                select * from configuration
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
                delete from configuration
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
