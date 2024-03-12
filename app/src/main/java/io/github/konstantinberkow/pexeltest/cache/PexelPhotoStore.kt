package io.github.konstantinberkow.pexeltest.cache

interface PexelPhotoStore {

    fun addPhoto(photo: DbPhoto)

    fun addPhotos(photos: List<DbPhoto>)

    fun replacePhotos(newPhotos: List<DbPhoto>)

    fun getCuratedPhotos(specifier: SizeSpecifier): List<DbPhotoWithUrl>

    fun getPhotoWithOriginal(id: Long): DbPhotoWithUrl
}

data class DbPhotoWithUrl(
    val id: Long,
    val authorName: String,
    val averageColor: Int,
    val imageUrl: String,
)
