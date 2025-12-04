package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType
import java.time.OffsetDateTime

@Repository
class ConfigSchemaEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigSchemaEntityRepository {
    companion object {
        private val ROW_MAPPER_DETAILED =
            RowMapper<ConfigSchemaDetailedEntity> { rs, _ ->
                ConfigSchemaDetailedEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configurationId = rs.getLong("configuration_id"),
                    sourceType = SourceType.valueOf(rs.getString("source_type")),
                    sourceIdentity = rs.getString("source_identity"),
                    jsonSchema = rs.getString("json_schema"),
                )
            }

        private val ROW_MAPPER_PARTIAL =
            RowMapper<ConfigSchemaEntity> { rs, _ ->
                ConfigSchemaEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configurationId = rs.getLong("configuration_id"),
                    sourceType = SourceType.valueOf(rs.getString("source_type")),
                    sourceIdentity = rs.getString("source_identity"),
                )
            }
    }

    override fun save(configSchemaDetailedEntity: ConfigSchemaDetailedEntity): ConfigSchemaDetailedEntity =
        npJdbc
            .query(
                """
                insert into config_schema (configuration_id, source_type, source_identity, json_schema)
                values (:configuration_id, :source_type, :source_identity, :json_schema::jsonb)
                returning *;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configSchemaDetailedEntity.configurationId,
                    "source_type" to configSchemaDetailedEntity.sourceType.toString(),
                    "source_identity" to configSchemaDetailedEntity.sourceIdentity,
                    "json_schema" to configSchemaDetailedEntity.jsonSchema,
                ),
                ROW_MAPPER_DETAILED,
            ).first()

    override fun findAllByConfigurationId(configurationId: Long): List<ConfigSchemaEntity> =
        npJdbc
            .query(
                """
                select id, created_at, configuration_id, source_type, source_identity, json_schema from config_schema
                where configuration_id = :configuration_id;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationId,
                ),
                ROW_MAPPER_PARTIAL,
            )

    override fun findById(id: Long): ConfigSchemaDetailedEntity? =
        npJdbc
            .query(
                """
                select * from config_schema
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
                ROW_MAPPER_DETAILED,
            ).firstOrNull()

    override fun removeById(id: Long): Boolean =
        npJdbc
            .update(
                """
                delete from config_schema
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
