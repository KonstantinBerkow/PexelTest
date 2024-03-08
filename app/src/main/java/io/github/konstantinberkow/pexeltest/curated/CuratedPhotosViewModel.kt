package io.github.konstantinberkow.pexeltest.curated

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import io.github.konstantinberkow.pexeltest.network.PexelPhotoPage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val PAGE_SIZE = 15

private const val TAG = "CuratedPhotosViewModel"

class CuratedPhotosViewModel(
    private val pexelApiProvider: () -> PexelApi
) : ViewModel() {

    private val state = MutableLiveData<CuratedPhotosState>(
        CuratedPhotosState(
            loadedPhotos = emptyList(),
            currentPage = 0,
            hasMore = true,
            status = CuratedPhotosState.Status.IDLE
        )
    )

    fun observePhotos(): LiveData<ViewState> {
        loadMoreIfEmptyAndIdle()
        return state.map { internalState ->
            val errorMsg = internalState.error?.let {
                when (it) {
                    CuratedPhotosState.Error.NO_CONNECTION -> "Failed to load content!"
                    CuratedPhotosState.Error.QUOTA_EXCEEDED -> "Quota exceeded, please wait"
                    CuratedPhotosState.Error.UNKNOWN -> "Unknown error"
                }
            }

            ViewState(
                photos = internalState.loadedPhotos,
                loadingMore = internalState.status == CuratedPhotosState.Status.LOADING,
                errorMsg = errorMsg
            )
        }
    }

    private fun loadMoreIfEmptyAndIdle() {
        val currentState = state.value ?: return
        if (currentState.loadedPhotos.isEmpty() && currentState.status == CuratedPhotosState.Status.IDLE && currentState.hasMore) {
            performLoad(currentState)
        }
    }

    fun loadMore() {
        val currentState = state.value ?: return
        if (currentState.hasMore && currentState.status == CuratedPhotosState.Status.IDLE) {
            performLoad(currentState)
        }
    }

    fun refresh() {
        val currentState = state.value ?: return
        // add code
    }

    private fun performLoad(currentState: CuratedPhotosState) {
        val nextPage = currentState.currentPage + 1
        val transientState = currentState.copy(
            error = null,
            currentPage = nextPage,
            status = CuratedPhotosState.Status.LOADING
        )
        state.value = transientState

        pexelApiProvider().curatedPhotos(
            nextPage,
            PAGE_SIZE
        ).enqueue(object : Callback<PexelPhotoPage> {
            override fun onResponse(
                call: Call<PexelPhotoPage>,
                response: Response<PexelPhotoPage>
            ) {
                Log.d(TAG, "Response code: ${response.code()}, message: ${response.message()}")
                response.body()?.let { body ->
                    Log.d(TAG, "success thread: ${Thread.currentThread()}")
                    val newState = transientState.copy(
                        currentPage = body.page,
                        loadedPhotos = body.photos.map(ToViewPhoto),
                        status = CuratedPhotosState.Status.IDLE
                    )
                    state.postValue(newState)
                }
            }

            override fun onFailure(call: Call<PexelPhotoPage>, error: Throwable) {
                Log.e(TAG, "Failed to get curated photos", error)
                Log.d(TAG, "failure thread: ${Thread.currentThread()}")
            }
        })
    }

    data class ViewState(
        val photos: List<PexelPhotoItem>,
        val loadingMore: Boolean,
        val errorMsg: String? = null
    )
}

object ToViewPhoto : (PexelPhoto) -> PexelPhotoItem {
    override fun invoke(fullPhoto: PexelPhoto): PexelPhotoItem {
        return PexelPhotoItem(
            id = fullPhoto.id,
            photographerName = fullPhoto.photographer,
            srcSmall = fullPhoto.src["small"] ?: "",
            srcLarge = fullPhoto.src["large"] ?: "",
            averageColor = fullPhoto.averageColor
        )
    }
}

private data class CuratedPhotosState(
    val loadedPhotos: List<PexelPhotoItem>,
    val error: Error? = null,
    val currentPage: Int,
    val hasMore: Boolean,
    val status: Status
) {

    enum class Status {
        IDLE,
        LOADING
    }

    enum class Error {
        NO_CONNECTION,
        QUOTA_EXCEEDED,
        UNKNOWN
    }
}
