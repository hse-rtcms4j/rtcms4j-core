package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import java.time.OffsetDateTime

@Repository
class ConfigurationCommitEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigurationCommitEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ConfigurationCommitEntity> { rs, _ ->
                ConfigurationCommitEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configurationId = rs.getLong("configuration_id"),
                    sourceType = SourceType.valueOf(rs.getString("source_type")),
                    sourceIdentity = rs.getString("source_identity"),
                    commitHash = rs.getString("commit_hash"),
                    jsonValues = rs.getString("json_values"),
                    jsonSchema = rs.getString("json_schema"),
                )
            }
    }

    /**
     * @throws DuplicateKeyException commit hash duplication
     * @throws DataIntegrityViolationException configuration does not exist
     */
    override fun save(configurationCommitEntity: ConfigurationCommitEntity): ConfigurationCommitEntity =
        npJdbc
            .query(
                """
                insert into configuration_commit (configuration_id, source_type, source_identity, commit_hash, json_values, json_schema)
                values (:configuration_id, :source_type, :source_identity, :commit_hash, :json_values::jsonb, :json_schema::jsonb)
                returning *;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationCommitEntity.configurationId,
                    "source_type" to configurationCommitEntity.sourceType.toString(),
                    "source_identity" to configurationCommitEntity.sourceIdentity,
                    "commit_hash" to configurationCommitEntity.commitHash,
                    "json_values" to configurationCommitEntity.jsonValues,
                    "json_schema" to configurationCommitEntity.jsonSchema,
                ),
                ROW_MAPPER,
            ).first()

    override fun findAllByConfigurationId(configurationId: Long): List<ConfigurationCommitEntity> =
        npJdbc
            .query(
                """
                select * from configuration_commit
                where configuration_id = :configuration_id;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationId,
                ),
                ROW_MAPPER,
            )

    override fun findByConfigurationIdAndCommitHash(
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitEntity? =
        npJdbc
            .query(
                """
                select * from configuration_commit
                where configuration_id = :configuration_id and
                      commit_hash = :commit_hash;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationId,
                    "commit_hash" to commitHash,
                ),
                ROW_MAPPER,
            ).firstOrNull()

    override fun findById(id: Long): ConfigurationCommitEntity? =
        npJdbc
            .query(
                """
                select * from configuration_commit
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
                delete from configuration_commit
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
