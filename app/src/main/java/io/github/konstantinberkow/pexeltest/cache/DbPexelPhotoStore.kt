package io.github.konstantinberkow.pexeltest.cache

import android.util.Log
import io.github.konstantinberkow.pexeltest.Database
import io.github.konstantinberkow.pexeltest.ImageUrlQueries
import io.github.konstantinberkow.pexeltest.PhotoQueries
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

private const val TAG = "DbPexelPhotoStore"

class DbPexelPhotoStore(
    databaseProvider: () -> Database,
    private val imageUrlSaveAdapter: ImageUrlSaveContract,
    private val dbCoroutineContext: CoroutineContext
) : PexelPhotoStore {

    interface ImageUrlSaveContract {

        fun extractPartToSave(url: String): String

        fun restoreImageUrl(photoId: Long, savedPart: String): String
    }

    private val database: Database by lazy(databaseProvider)

    private val mapper = MapSelectResultToPhotoWirthUrl(imageUrlSaveAdapter)

    // must be in transaction
    private fun performPhotoInsert(
        photoQueries: PhotoQueries,
        imageUrlQueries: ImageUrlQueries,
        photo: DbPhoto,
        deleteOldUrls: Boolean = false
    ) {
        val photoId = photo.id
        photoQueries.insert(photoId, photo.authorName, photo.averageColor.toLong())
        if (deleteOldUrls) {
            imageUrlQueries.deleteByPhotoId(photoId)
        }
        photo.src.forEach { (key, url) ->
            SizeSpecifier.fromString(key)?.let { specifier ->
                val partToSave = imageUrlSaveAdapter.extractPartToSave(url)
                Log.v(TAG, "Image url part to save: $partToSave")
                imageUrlQueries.insert(photoId, specifier.longValue, partToSave)
            }
        }
    }

    override suspend fun addPhoto(photo: DbPhoto) {
        withContext(dbCoroutineContext) {
            with(database) {
                transaction {
                    performPhotoInsert(photoQueries, imageUrlQueries, photo, true)
                }
            }
        }
    }

    override suspend fun addPhotos(photos: List<DbPhoto>) {
        withContext(dbCoroutineContext) {
            with(database) {
                val photoQueries = photoQueries
                val imageUrlQueries = imageUrlQueries
                transaction {
                    photos.forEach { photo ->
                        performPhotoInsert(photoQueries, imageUrlQueries, photo, true)
                    }
                }
            }
        }
    }

    override suspend fun replacePhotos(newPhotos: List<DbPhoto>) {
        withContext(dbCoroutineContext) {
            with(database) {
                val photoQueries = photoQueries
                val imageUrlQueries = imageUrlQueries
                transaction {
                    photoQueries.deleteAll()
                    imageUrlQueries.deleteAll()

                    newPhotos.forEach { photo ->
                        performPhotoInsert(photoQueries, imageUrlQueries, photo)
                    }
                }
            }
        }
    }

    override suspend fun getCuratedPhotos(specifier: SizeSpecifier): List<DbPhotoWithUrl> {
        return withContext(dbCoroutineContext) {
            database.photoQueries
                .selectPhotosForQualifier(specifier.longValue, mapper)
                .executeAsList()
        }
    }

    override suspend fun getPhotoWithOriginal(id: Long): DbPhotoWithUrl {
        return withContext(dbCoroutineContext) {
            database.photoQueries
                .selectPhotoForQualifier(
                    id = id,
                    qualifier = SizeSpecifier.ORIGINAL.longValue,
                    mapper = mapper
                )
                .executeAsOne()
        }
    }
}

private class MapSelectResultToPhotoWirthUrl(
    private val imageUrlSaveAdapter: DbPexelPhotoStore.ImageUrlSaveContract
) : (Long, String, Long, String) -> DbPhotoWithUrl {

    override fun invoke(
        id: Long,
        authorName: String,
        averageColor: Long,
        query: String
    ): DbPhotoWithUrl {
        val restoredUrl = imageUrlSaveAdapter.restoreImageUrl(id, query)
        Log.v(TAG, "Restored url: $restoredUrl")
        return DbPhotoWithUrl(
            id = id,
            authorName = authorName,
            averageColor = averageColor.toInt(),
            imageUrl = restoredUrl
        )
    }
}
