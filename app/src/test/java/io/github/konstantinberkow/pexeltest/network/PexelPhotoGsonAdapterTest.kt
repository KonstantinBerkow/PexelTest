package io.github.konstantinberkow.pexeltest.network

import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Test

class PexelPhotoGsonAdapterTest {

    private val androidParseColor = { colorStr: String ->
        var color: Long = colorStr.substring(1).toLong(16)
        // Set the alpha value
        color = color or 0x00000000ff000000L
        color.toInt()
    }

    private val adapter = PexelPhotoGsonAdapter(
        parseColor = androidParseColor
    )

    private val gson = GsonBuilder()
        .registerTypeAdapter(PexelPhoto::class.java, adapter)
        .create()

    @Test
    fun `verify read`() {
        val id = 100500L
        val photographer = "Artur Stec"
        val averageColor = "#7F6355"
        val key1 = "original"
        val srcUrl1 = "https://images.pexels.com/photos/$id?s=1"
        val key2 = "small"
        val srcUrl2 = "https://images.pexels.com/photos/$id?s=2"

        val expected = PexelPhoto(
            id = id,
            photographer = photographer,
            averageColor = androidParseColor(averageColor),
            src = mapOf(key1 to srcUrl1, key2 to srcUrl2)
        )

        val sampleJson = """
            {
                "id": $id,
                "photographer": "$photographer",
                "avg_color": "$averageColor",
                "src": {
                    "$key1": "$srcUrl1",
                    "$key2": "$srcUrl2"
                },
                alt: "gibberish"
            }
        """.trimIndent()

        val result = gson.fromJson(sampleJson, PexelPhoto::class.java)

        assertEquals(
            expected,
            result
        )
    }
}
