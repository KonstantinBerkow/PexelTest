package io.github.konstantinberkow.pexeltest.cache

import android.util.Log

private const val TAG = "PexelPhotoStore"

interface PexelPhotoStore {

    fun addPhoto(photo: DbPhoto)

    fun addPhotos(photos: List<DbPhoto>)

    fun replacePhotos(newPhotos: List<DbPhoto>)

    fun getCuratedPhotos(specifier: SizeSpecifier): List<DbPhotoWithUrl>

    fun getPhotoWithOriginal(id: Long): DbPhotoWithUrl
}

class PexelPhotoStoreLoggingProxy(
    private val delegate: PexelPhotoStore
) : PexelPhotoStore {

    override fun addPhoto(photo: DbPhoto) {
        Log.d(TAG, "addPhoto: $photo, thread: ${Thread.currentThread()}")
        delegate.addPhoto(photo)
    }

    override fun addPhotos(photos: List<DbPhoto>) {
        Log.d(TAG, "addPhotos: $photos, thread: ${Thread.currentThread()}")
        delegate.addPhotos(photos)
    }

    override fun replacePhotos(newPhotos: List<DbPhoto>) {
        Log.d(TAG, "replacePhotos: $newPhotos, thread: ${Thread.currentThread()}")
        delegate.replacePhotos(newPhotos)
    }

    override fun getCuratedPhotos(specifier: SizeSpecifier): List<DbPhotoWithUrl> {
        Log.d(TAG, "getCuratedPhotos: $specifier, thread: ${Thread.currentThread()}")
        return delegate.getCuratedPhotos(specifier)
    }

    override fun getPhotoWithOriginal(id: Long): DbPhotoWithUrl {
        Log.d(TAG, "getPhotoWithOriginal: $id, thread: ${Thread.currentThread()}")
        return delegate.getPhotoWithOriginal(id)
    }
}

data class DbPhotoWithUrl(
    val id: Long,
    val authorName: String,
    val averageColor: Int,
    val imageUrl: String,
)
