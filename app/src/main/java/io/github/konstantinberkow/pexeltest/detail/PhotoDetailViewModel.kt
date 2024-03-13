package io.github.konstantinberkow.pexeltest.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.PexelTestApp
import io.github.konstantinberkow.pexeltest.cache.DbPhoto
import io.github.konstantinberkow.pexeltest.cache.PexelPhotoStore
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val TAG = "PhotoDetailViewModel"

class PhotoDetailViewModel(
    private val pexelApi: PexelApi,
    private val photoStore: PexelPhotoStore
) : ViewModel() {

    private val state: MutableLiveData<ViewState> = MutableLiveData(
        ViewState(
            photoId = 0L,
            photoDetail = null,
            loading = false
        )
    )

    fun getPhoto(photoId: Long): LiveData<ViewState> {
        loadIfIdle(photoId)
        return state
    }

    data class ViewState(
        val photoId: Long,
        val photoDetail: PhotoDetail?,
        val loading: Boolean,
        val errorMsg: String? = null
    )

    private fun loadIfIdle(photoId: Long) {
        val oldState = state.value ?: return

        val oldPhoto = oldState.photoDetail
        if ((oldPhoto != null && oldPhoto.id == photoId) || oldState.loading) {
            return
        }

        val transientState = oldState.copy(
            photoId = photoId,
            loading = true,
            errorMsg = null
        )
        state.value = transientState

        viewModelScope.launch {
            launch(Dispatchers.IO) {
                val dbPhoto = photoStore.getPhotoWithOriginal(id = photoId)
                val cachedData = PhotoDetail(
                    id = dbPhoto.id,
                    originalImageUrl = dbPhoto.imageUrl,
                    authorName = dbPhoto.authorName,
                    authorUrl = null
                )
                val newState = transientState.copy(
                    photoDetail = cachedData
                )
                state.postValue(newState)

                performNetworkLoad(newState)
            }
        }
    }

    private suspend fun performNetworkLoad(oldState: ViewState) {
        val newState = try {
            val pexelPhoto = pexelApi.getPhotoInfo(oldState.photoId)
            val newState = oldState.copy(
                photoDetail = pexelPhoto.toPhotoDetail(),
                loading = false,
                errorMsg = null,
            )

            photoStore.addPhoto(
                DbPhoto(
                    id = pexelPhoto.id,
                    authorName = pexelPhoto.photographer,
                    averageColor = pexelPhoto.averageColor,
                    src = pexelPhoto.src
                )
            )

            newState
        } catch (e: HttpException) {
            oldState.copy(
                loading = false,
                errorMsg = "HTTP: ${e.code()}"
            )
        }

        state.postValue(newState)
    }

    object Factory : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass == PhotoDetailViewModel::class.java)
            val app =
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PexelTestApp

            return PhotoDetailViewModel(
                pexelApi = app.dependenciesContainer.pexelApi,
                photoStore = app.dependenciesContainer.pexelPhotoStore,
            ) as T
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
