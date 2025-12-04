package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.SourceType
import java.time.OffsetDateTime

@Repository
class ConfigCommitEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigCommitEntityRepository {
    companion object {
        private val ROW_MAPPER_DETAILED =
            RowMapper<ConfigCommitDetailedEntity> { rs, _ ->
                ConfigCommitDetailedEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configSchemaId = rs.getLong("config_schema_id"),
                    configurationId = rs.getLong("configuration_id"),
                    sourceType = SourceType.valueOf(rs.getString("source_type")),
                    sourceIdentity = rs.getString("source_identity"),
                    jsonValues = rs.getString("json_values"),
                )
            }

        private val ROW_MAPPER_PARTIAL =
            RowMapper<ConfigCommitEntity> { rs, _ ->
                ConfigCommitEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configSchemaId = rs.getLong("config_schema_id"),
                    configurationId = rs.getLong("configuration_id"),
                    sourceType = SourceType.valueOf(rs.getString("source_type")),
                    sourceIdentity = rs.getString("source_identity"),
                )
            }
    }

    override fun save(configCommitDetailedEntity: ConfigCommitDetailedEntity): ConfigCommitDetailedEntity =
        npJdbc
            .query(
                """
                insert into config_commit (config_schema_id, configuration_id, source_type, source_identity, json_values)
                values (:config_schema_id, :configuration_id, :source_type, :source_identity, :json_values::jsonb)
                returning *;
                """.trimIndent(),
                mapOf(
                    "config_schema_id" to configCommitDetailedEntity.configSchemaId,
                    "configuration_id" to configCommitDetailedEntity.configurationId,
                    "source_type" to configCommitDetailedEntity.sourceType.toString(),
                    "source_identity" to configCommitDetailedEntity.sourceIdentity,
                    "json_values" to configCommitDetailedEntity.jsonValues,
                ),
                ROW_MAPPER_DETAILED,
            ).first()

    override fun findAllByConfigSchemaId(configSchemaId: Long): List<ConfigCommitEntity> =
        npJdbc
            .query(
                """
                select id, created_at, config_schema_id, configuration_id, source_type, source_identity, json_values from config_commit
                where config_schema_id = :config_schema_id;
                """.trimIndent(),
                mapOf(
                    "config_schema_id" to configSchemaId,
                ),
                ROW_MAPPER_PARTIAL,
            )

    override fun findAllByConfigurationId(configurationId: Long): List<ConfigCommitEntity> =
        npJdbc
            .query(
                """
                select id, created_at, config_schema_id, configuration_id, source_type, source_identity, json_values from config_commit
                where configuration_id = :configuration_id;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationId,
                ),
                ROW_MAPPER_PARTIAL,
            )

    override fun findById(id: Long): ConfigCommitDetailedEntity? =
        npJdbc
            .query(
                """
                select * from config_commit
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
                delete from config_commit
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
