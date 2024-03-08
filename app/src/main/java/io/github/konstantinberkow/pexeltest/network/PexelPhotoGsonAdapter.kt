package io.github.konstantinberkow.pexeltest.network

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

private const val ID_KEY = "id"
private const val PHOTOGRAPHER_KEY = "photographer"
private const val PHOTOGRAPHER_URL_KEY = "photographer_url"
private const val AVERAGE_COLOR_KEY = "avg_color"
private const val SRC_KEY = "src"
//private const val SMALL_SRC_KEY = "small"
//private const val MEDIUM_SRC_KEY = "medium"
//private const val LARGE_SRC_KEY = "large"
//private const val ORIGINAL_SRC_KEY = "original"

class PexelPhotoGsonAdapter(
    private val parseColor: (String) -> Int
) : TypeAdapter<PexelPhoto>() {

    override fun write(writer: JsonWriter, value: PexelPhoto?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        with(writer) {
            beginObject()
            name(ID_KEY).value(value.id)
            name(PHOTOGRAPHER_KEY).value(value.photographer)

            if (value.src.isNotEmpty()) {
                name(SRC_KEY)
                beginObject()
                value.src.forEach { (key, url) ->
                    name(key).value(url)
                }
                endObject()
            }

            endObject()
        }
    }

    override fun read(reader: JsonReader): PexelPhoto? {
        if (reader.peek() == JsonToken.NULL) {
            return null
        }

        var id: Long = 0
        var photographer: String? = null
        var photographerUrl: String? = null
        var averageColor: String? = null
        var src: Map<String, String>? = null

        PHOTOGRAPHER_URL_KEY

        with(reader) {
            beginObject()

            while (reader.peek() != JsonToken.END_OBJECT) {
                when (reader.nextName()) {
                    ID_KEY -> id = nextLong()
                    PHOTOGRAPHER_KEY -> photographer = nextString()
                    AVERAGE_COLOR_KEY -> averageColor = nextString()
                    PHOTOGRAPHER_URL_KEY -> photographerUrl = nextString()
                    SRC_KEY -> {
                        beginObject()
                        src = mutableMapOf<String, String>().apply {
                            while (reader.peek() == JsonToken.NAME) {
                                put(nextName(), nextString())
                            }
                        }
                        endObject()
                    }

                    else -> skipValue()
                }
            }
            endObject()
        }

        if (id != 0L && photographer != null && photographerUrl != null && averageColor != null && src != null) {
            return PexelPhoto(
                id = id,
                photographer = photographer!!,
                photographerUrl = photographerUrl!!,
                averageColor = parseColor(averageColor!!),
                src = src!!
            )
        }

        return null
    }
}
