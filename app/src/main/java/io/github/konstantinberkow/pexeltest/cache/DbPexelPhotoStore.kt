package io.github.konstantinberkow.pexeltest.cache

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.konstantinberkow.pexeltest.Database
import io.github.konstantinberkow.pexeltest.ImageUrlQueries
import io.github.konstantinberkow.pexeltest.PhotoQueries

class DbPexelPhotoStore(
    context: Context,
    private val photoBasePath: String
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
        photoQueries.insert(photoId, photo.authorName, photo.averageColor)
        photo.src.forEach { (key, url) ->
            SizeSpecifier.fromString(key)?.let { specifier ->
                val path = url.removePrefix(photoBasePath)
                imageUrlQueries.insert(photoId, specifier.intValue.toLong(), path)
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
    private val photoBasePath: String
) : (Long, String, Long, String) -> DbPhotoWithUrl {
    override fun invoke(
        id: Long,
        authorName: String,
        averageColor: Long,
        path: String
    ): DbPhotoWithUrl {
        return DbPhotoWithUrl(
            id = id,
            authorName = authorName,
            averageColor = averageColor,
            imageUrl = photoBasePath + path
        )
    }
}
