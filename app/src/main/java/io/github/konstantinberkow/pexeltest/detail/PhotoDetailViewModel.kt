package io.github.konstantinberkow.pexeltest.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.PexelTestApp
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "PhotoDetailViewModel"

class PhotoDetailViewModel(
    private val pexelApiProvider: () -> PexelApi
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

        pexelApiProvider().getPhotoInfo(photoId).enqueue(object : Callback<PexelPhoto> {
            override fun onResponse(call: Call<PexelPhoto>, response: Response<PexelPhoto>) {
                response.body()?.let { body ->
                    val newState = transientState.copy(
                        photoDetail = body.toPhotoDetail(),
                        loading = false,
                        errorMsg = null,
                    )
                    state.value = newState
                }
            }

            override fun onFailure(call: Call<PexelPhoto>, error: Throwable) {
                Log.e(TAG, "Failed to get photo info", error)
            }
        })
    }

    object Factory : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass == PhotoDetailViewModel::class.java)
            val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PexelTestApp

            return PhotoDetailViewModel(
                pexelApiProvider = { app.dependenciesContainer.pexelApi }
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
