package io.github.konstantinberkow.pexeltest.network

import com.google.gson.GsonBuilder
import org.junit.Assert
import org.junit.Test

class PexelPhotoPageGsonAdapterTest {

    private val androidParseColor = { colorStr: String ->
        var color: Long = colorStr.substring(1).toLong(16)
        // Set the alpha value
        color = color or 0x00000000ff000000L
        color.toInt()
    }

    private val photoAdapter = PexelPhotoGsonAdapter(
        parseColor = androidParseColor
    )

    private val adapter = PexelPhotoPageGsonAdapter(photoAdapter)

    private val gson = GsonBuilder()
        .registerTypeAdapter(PexelPhoto::class.java, photoAdapter)
        .registerTypeAdapter(PexelPhotoPage::class.java, adapter)
        .create()

    @Test
    fun `verify read`() {
        val id1 = 100500L
        val id2 = 100501L
        val photographer1 = "John Doe #1"
        val photographer2 = "John Doe #2"
        val photographerUrl1 = "https://pexel.com/@john_doe_1"
        val photographerUrl2 = "https://pexel.com/@john_doe_2"
        val averageColor = "#7F6355"
        val key1 = "original"
        val srcUrl1 = "https://images.pexels.com/photos/abc?s=1"
        val key2 = "small"
        val srcUrl2 = "https://images.pexels.com/photos/abc?s=2"

        val expected = PexelPhotoPage(
            page = 1,
            photos = listOf(
                PexelPhoto(
                    id = id1,
                    photographer = photographer1,
                    photographerUrl = photographerUrl1,
                    averageColor = androidParseColor(averageColor),
                    src = mapOf(key1 to srcUrl1, key2 to srcUrl2)
                ),
                PexelPhoto(
                    id = id2,
                    photographer = photographer2,
                    photographerUrl = photographerUrl2,
                    averageColor = androidParseColor(averageColor),
                    src = mapOf(key1 to srcUrl1, key2 to srcUrl2)
                )
            )
        )

        val sampleJson = """
            {
              "page": 1,
              "per_page": 1,
              "photos": [
                {
                  "id": $id1,
                  "photographer": "$photographer1",
                  "photographer_url": "$photographerUrl1",
                  "avg_color": "$averageColor",
                  "src": {
                    "$key1": "$srcUrl1",
                    "$key2": "$srcUrl2"
                  },
                  "alt": "..."
                },
                {
                  "id": $id2,
                  "photographer": "$photographer2",
                  "photographer_url": "$photographerUrl2",
                  "avg_color": "$averageColor",
                  "src": {
                    "$key1": "$srcUrl1",
                    "$key2": "$srcUrl2"
                  },
                  "alt": "...-"
                }
              ],
              "next_page": "https://api.pexels.com/v1/curated/?page=2&per_page=1"
            }
        """.trimIndent()

        val result = gson.fromJson(sampleJson, PexelPhotoPage::class.java)

        Assert.assertEquals(
            expected,
            result
        )
    }
}