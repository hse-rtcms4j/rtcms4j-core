package unit.ru.enzhine.rtcms4j.core.json

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.enzhine.rtcms4j.core.json.CommitHashEvaluatorImpl

class CommitHashEvaluatorImplTest {
    private val commitHashEvaluator = CommitHashEvaluatorImpl()

    private val jsonValues1 =
        """
        {
          "featureFlag": false
        }
        """.trimIndent()

    private val jsonValues2 =
        """
        {
          "featureFlag": true
        }
        """.trimIndent()

    private val jsonSchema1 =
        $$"""
        {
          "$schema": "https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "additionalProperties": false,
          "properties": {
            "featureFlag": {
              "type": "boolean",
              "default": false
            }
          },
          "required": ["featureFlag"]
        }
        """.trimIndent()

    private val jsonSchema2 =
        $$"""
        {
          "$schema": "https://json-schema.org/draft/2020-12/schema",
        }
        """.trimIndent()

    @Test
    fun evalCommitHash_sameHash1_correct() {
        val hash = commitHashEvaluator.evalCommitHash(jsonValues1, jsonSchema1)
        val sameHash = commitHashEvaluator.evalCommitHash(jsonValues1, jsonSchema1)
        Assertions.assertEquals(hash, sameHash)
    }

    @Test
    fun evalCommitHash_sameHash2_correct() {
        val hash2 = commitHashEvaluator.evalCommitHash(jsonValues2, jsonSchema2)
        val sameHash2 = commitHashEvaluator.evalCommitHash(jsonValues2, jsonSchema2)
        Assertions.assertEquals(hash2, sameHash2)
    }

    @Test
    fun evalCommitHash_differentHash1_correct() {
        val hash = commitHashEvaluator.evalCommitHash(jsonValues1, jsonSchema1)
        val differentHash1 = commitHashEvaluator.evalCommitHash(jsonValues1, jsonSchema2)
        Assertions.assertNotEquals(hash, differentHash1)
    }

    @Test
    fun evalCommitHash_differentHash2_correct() {
        val hash = commitHashEvaluator.evalCommitHash(jsonValues1, jsonSchema1)
        val differentHash2 = commitHashEvaluator.evalCommitHash(jsonValues2, jsonSchema1)
        Assertions.assertNotEquals(hash, differentHash2)
    }
}
