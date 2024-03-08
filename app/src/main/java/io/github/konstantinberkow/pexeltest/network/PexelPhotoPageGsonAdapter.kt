package io.github.konstantinberkow.pexeltest.network

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

private const val PAGE_KEY = "page"
private const val PHOTOS_KEY = "photos"

class PexelPhotoPageGsonAdapter(private val photoAdapter: TypeAdapter<PexelPhoto>) :
    TypeAdapter<PexelPhotoPage>() {

    override fun write(out: JsonWriter, value: PexelPhotoPage?) {
        TODO("Not yet implemented")
    }

    override fun read(reader: JsonReader): PexelPhotoPage? {
        if (reader.peek() == JsonToken.NULL) {
            return null
        }

        var page = -1
        var photos: List<PexelPhoto>? = null

        reader.beginObject()
        while (reader.peek() != JsonToken.END_OBJECT) {
            when (reader.nextName()) {
                PAGE_KEY -> page = reader.nextInt()
                PHOTOS_KEY -> {
                    reader.beginArray()
                    photos = mutableListOf<PexelPhoto>().apply {
                        while (reader.peek() != JsonToken.END_ARRAY) {
                            add(photoAdapter.read(reader))
                        }
                    }
                    reader.endArray()
                }

                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (page != -1 && photos != null) {
            return PexelPhotoPage(page, photos)
        }

        return null
    }
}
