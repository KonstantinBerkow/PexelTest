package io.github.konstantinberkow.pexeltest.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.PexelTestApp
import io.github.konstantinberkow.pexeltest.cache.DbPhoto
import io.github.konstantinberkow.pexeltest.cache.DbPhotoWithUrl
import io.github.konstantinberkow.pexeltest.cache.PexelPhotoStore
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException

private const val TAG = "PhotoDetailViewModel"

sealed interface PhotoDetailState {

    val photoId: Long

    data object Initial : PhotoDetailState {

        override val photoId: Long
            get() = 0L

        override fun reduce(result: PhotoDetailResult): PhotoDetailState {
            return when (result) {
                is PhotoDetailResult.Failure ->
                    Error(
                        photoId = photoId,
                        errorMsg = result.errorMsg,
                        loadedPhoto = null
                    )

                is PhotoDetailResult.Success ->
                    Idle(
                        photo = result.photo
                    )

                is PhotoDetailResult.Loading ->
                    Loading(
                        photoId = result.photoId
                    )
            }
        }
    }

    data class Loading(override val photoId: Long) : PhotoDetailState {

        override fun reduce(result: PhotoDetailResult): PhotoDetailState {
            return when (result) {
                is PhotoDetailResult.Failure ->
                    Error(
                        photoId = photoId,
                        errorMsg = result.errorMsg,
                        loadedPhoto = null
                    )

                is PhotoDetailResult.Success ->
                    Idle(photo = result.photo)

                is PhotoDetailResult.Loading ->
                    if (photoId == result.photoId) {
                        this
                    } else {
                        Loading(photoId = photoId)
                    }
            }
        }
    }

    data class Refreshing(val photo: PhotoDetail) : PhotoDetailState {

        override val photoId: Long
            get() = photo.id

        override fun reduce(result: PhotoDetailResult): PhotoDetailState {
            return when (result) {
                is PhotoDetailResult.Failure ->
                    Error(
                        photoId = photoId,
                        errorMsg = result.errorMsg,
                        loadedPhoto = photo
                    )

                is PhotoDetailResult.Success ->
                    Idle(photo = result.photo)

                is PhotoDetailResult.Loading ->
                    if (photoId == result.photoId) {
                        this
                    } else {
                        Loading(photoId = photoId)
                    }
            }
        }
    }

    data class Idle(val photo: PhotoDetail) : PhotoDetailState {

        override val photoId: Long
            get() = photo.id

        override fun reduce(result: PhotoDetailResult): PhotoDetailState {
            return when (result) {
                is PhotoDetailResult.Failure ->
                    Error(
                        photoId = photoId,
                        errorMsg = result.errorMsg,
                        loadedPhoto = photo
                    )

                is PhotoDetailResult.Success ->
                    Idle(photo = result.photo)

                is PhotoDetailResult.Loading ->
                    if (photoId == result.photoId) {
                        Refreshing(photo = photo)
                    } else {
                        Loading(photoId = result.photoId)
                    }
            }
        }
    }

    data class Error(
        override val photoId: Long,
        val errorMsg: String,
        val loadedPhoto: PhotoDetail?
    ) : PhotoDetailState {

        override fun reduce(result: PhotoDetailResult): PhotoDetailState {
            return when (result) {
                is PhotoDetailResult.Failure ->
                    if (errorMsg == result.errorMsg) {
                        this
                    } else {
                        Error(
                            photoId = photoId,
                            errorMsg = result.errorMsg,
                            loadedPhoto = loadedPhoto
                        )
                    }

                is PhotoDetailResult.Success ->
                    Idle(photo = result.photo)

                is PhotoDetailResult.Loading ->
                    if (photoId == result.photoId && loadedPhoto != null) {
                        Refreshing(photo = loadedPhoto)
                    } else {
                        Loading(photoId = result.photoId)
                    }
            }
        }
    }

    fun reduce(result: PhotoDetailResult): PhotoDetailState
}

sealed interface PhotoDetailResult {

    data class Loading(val photoId: Long) : PhotoDetailResult

    data class Success(val photo: PhotoDetail) : PhotoDetailResult

    data class Failure(val errorMsg: String) : PhotoDetailResult
}

inline fun log(tag: String, msg: () -> String) {
    Log.d(tag, msg())
}

class PhotoDetailViewModel(
    private val pexelApi: PexelApi,
    private val photoStore: PexelPhotoStore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val photoDetailResultsFlow = savedStateHandle.getStateFlow("photo_id", 0L)
        .filter { it > 0 }
        .onEach {
            log(TAG) { "next action: Load($it)" }
        }
        .flatMapLatest { photoId ->
            flow {
                val loading = PhotoDetailResult.Loading(photoId = photoId)
                emit(loading)
                emit(photoStore.loadFromDb(photoId))
                emit(loading)
                try {
                    val freshData = pexelApi.getPhotoInfo(photoId)
                    emit(PhotoDetailResult.Success(freshData.toPhotoDetail()))
                    photoStore.savePhotoQuietly(freshData)
                } catch (e: HttpException) {
                    emit(PhotoDetailResult.Failure(errorMsg = "HTTP ${e.code()}"))
                } catch (e: TimeoutException) {
                    emit(PhotoDetailResult.Failure(errorMsg = "Timeout"))
                } catch (e: IOException) {
                    emit(PhotoDetailResult.Failure(errorMsg = e.message ?: "IO error"))
                }
            }
        }
        .onEach {
            log(TAG) { "next result: $it" }
        }
        .scan(PhotoDetailState.Initial as PhotoDetailState) { acc, result ->
            acc.reduce(result)
        }
        .onEach {
            log(TAG) { "next state: $it" }
        }
        .catch { exception ->
            log(TAG) { "unhandled exception: $exception" }
        }

    private val hiddenState = MutableLiveData<PhotoDetailState>()

    val exposedState: LiveData<PhotoDetailState>
        get() = hiddenState

    fun getPhoto(photoId: Long) {
        savedStateHandle["photo_id"] = photoId

        viewModelScope.launch {
            photoDetailResultsFlow
                .collect {
                    hiddenState.value = it
                }
        }
    }

    object Factory : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass == PhotoDetailViewModel::class.java)
            val app =
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PexelTestApp
            val savedStateHandle = extras.createSavedStateHandle()
            val deps = app.dependenciesContainer
            return PhotoDetailViewModel(
                pexelApi = deps.pexelApi,
                photoStore = deps.pexelPhotoStore,
                savedStateHandle = savedStateHandle,
            ) as T
        }
    }
}

private suspend fun PexelPhotoStore.loadFromDb(photoId: Long): PhotoDetailResult {
    return try {
        val cachedData = getPhotoWithOriginal(photoId)
        PhotoDetailResult.Success(cachedData.toPhotoDetail())
    } catch (e: Exception) {
        if (e is CancellationException) {
            throw e
        } else {
            PhotoDetailResult.Failure("Cache get failed: ${e.message}")
        }
    }
}

private suspend fun PexelPhotoStore.savePhotoQuietly(photo: PexelPhoto) {
    try {
        addPhoto(
            DbPhoto(
                id = photo.id,
                authorName = photo.photographer,
                averageColor = photo.averageColor,
                src = photo.src
            )
        )
    } catch (e: Exception) {
        if (e is CancellationException) {
            throw e
        } else {
            Log.e(TAG, "Failed to update photo[${photo.id}]: ${e.message}")
        }
    }
}

private fun PexelPhoto.toPhotoDetail(): PhotoDetail {
    return PhotoDetail(
        id = id,
        originalImageUrl = src["original"] ?: "",
        authorName = photographer,
        authorUrl = photographerUrl
    )
}

private fun DbPhotoWithUrl.toPhotoDetail(): PhotoDetail {
    return PhotoDetail(
        id = id,
        originalImageUrl = imageUrl,
        authorName = authorName,
        authorUrl = null
    )
}
