package io.github.konstantinberkow.pexeltest.cache

import android.content.Context
import android.net.Uri
import android.util.Log
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.konstantinberkow.pexeltest.Database
import io.github.konstantinberkow.pexeltest.ImageUrlQueries
import io.github.konstantinberkow.pexeltest.PhotoQueries

private const val TAG = "DbPexelPhotoStore"

class DbPexelPhotoStore(
    context: Context,
    photoBasePath: String
) : PexelPhotoStore {

    private val database: Database by lazy {
        val driver = AndroidSqliteDriver(Database.Schema, context, "pexel_photos.db")
        Database(driver)
    }

    private val mapper = MapSelectResultToPhotoWirthUrl(photoBasePath)

    // must be in transaction
    private fun performPhotoInsert(
        photoQueries: PhotoQueries,
        imageUrlQueries: ImageUrlQueries,
        photo: DbPhoto
    ) {
        val photoId = photo.id
        photoQueries.insert(photoId, photo.authorName, photo.averageColor.toLong())
        photo.src.forEach { (key, url) ->
            SizeSpecifier.fromString(key)?.let { specifier ->
                // photoBasePath + "/:id/pexels-photo-20423561.jpeg?query=...
                val query = Uri.parse(url).query ?: ""
                Log.v(TAG, "extracted query: $query")
                imageUrlQueries.insert(photoId, specifier.intValue.toLong(), query)
            }
        }
    }

    override fun addPhoto(photo: DbPhoto) {
        val photoQueries = database.photoQueries
        val imageUrlQueries = database.imageUrlQueries
        database.transaction {
            performPhotoInsert(photoQueries, imageUrlQueries, photo)
        }
    }

    override fun addPhotos(photos: List<DbPhoto>) {
        val photoQueries = database.photoQueries
        val imageUrlQueries = database.imageUrlQueries
        database.transaction {
            photos.forEach { photo ->
                performPhotoInsert(photoQueries, imageUrlQueries, photo)
            }
        }
    }

    override fun replacePhotos(newPhotos: List<DbPhoto>) {
        val photoQueries = database.photoQueries
        val imageUrlQueries = database.imageUrlQueries
        database.transaction {
            photoQueries.deleteAll()
            imageUrlQueries.deleteAll()

            newPhotos.forEach { photo ->
                performPhotoInsert(photoQueries, imageUrlQueries, photo)
            }
        }
    }

    override fun getCuratedPhotos(specifier: SizeSpecifier): List<DbPhotoWithUrl> {
        return database.photoQueries
            .selectPhotosForQualifier(specifier.intValue.toLong(), mapper)
            .executeAsList()
    }

    override fun getPhotoWithOriginal(id: Long): DbPhotoWithUrl {
        return database.photoQueries
            .selectPhotoForQualifier(
                id = id,
                qualifier = SizeSpecifier.ORIGINAL.intValue.toLong(),
                mapper = mapper
            )
            .executeAsOne()
    }
}

private class MapSelectResultToPhotoWirthUrl(
    photoBasePath: String
) : (Long, String, Long, String) -> DbPhotoWithUrl {

    private val baseUri = Uri.parse(photoBasePath)

    override fun invoke(
        id: Long,
        authorName: String,
        averageColor: Long,
        query: String
    ): DbPhotoWithUrl {
        // photoBasePath + "/:id/pexels-photo-:id.jpeg?query=...
        val strId = id.toString()
        val fullUrl = baseUri.buildUpon()
            .appendPath(strId)
            .appendPath("pexels-photo-$strId.jpeg")
            .encodedQuery(query)
            .build()
        val restoredUrl = fullUrl.toString()
        Log.v(TAG, "Restored url: $restoredUrl")
        return DbPhotoWithUrl(
            id = id,
            authorName = authorName,
            averageColor = averageColor.toInt(),
            imageUrl = restoredUrl
        )
    }
}
