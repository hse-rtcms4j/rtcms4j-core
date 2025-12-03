package unit.ru.enzhine.rtcms4j.core.json

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.enzhine.rtcms4j.core.json.JsonNormalizerImpl

class JsonNormalizerImplTest {
    private val jsonNormalizer = JsonNormalizerImpl()

    private val jsonNormalized = """{"a":"b","b":"a","key1":true,"key2":false}"""

    private val jsonValues1 =
        """
        {
          "a": "b",
          "b": "a",
          "empty": null,
          "key1": true,
          "key2": false
        }
        """.trimIndent()

    private val jsonValues2 =
        """
        {
          "key2": false,
          "b": "a",
          "a": "b",
          "key1": true,
          "empty": null
        }
        """.trimIndent()

    @Test
    fun evalCommitHash_normalized_normalized() {
        val normalized = jsonNormalizer.normalize(jsonNormalized)
        Assertions.assertEquals(jsonNormalized, normalized)
    }

    @Test
    fun evalCommitHash_ordered_normalized() {
        val normalized1 = jsonNormalizer.normalize(jsonValues1)
        Assertions.assertEquals(jsonNormalized, normalized1)
    }

    @Test
    fun evalCommitHash_unordered_normalized() {
        val normalized2 = jsonNormalizer.normalize(jsonValues2)
        Assertions.assertEquals(jsonNormalized, normalized2)
    }
}
