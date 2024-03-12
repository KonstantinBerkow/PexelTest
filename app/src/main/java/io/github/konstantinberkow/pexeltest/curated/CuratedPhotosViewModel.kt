package io.github.konstantinberkow.pexeltest.curated

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.PexelTestApp
import io.github.konstantinberkow.pexeltest.cache.DbPhotoWithUrl
import io.github.konstantinberkow.pexeltest.cache.PexelPhotoStore
import io.github.konstantinberkow.pexeltest.cache.SizeSpecifier
import io.github.konstantinberkow.pexeltest.data.PhotoMediator
import java.util.concurrent.Executor

private const val TAG = "CuratedPhotosViewModel"

class CuratedPhotosViewModel(
    private val photoStore: PexelPhotoStore,
    private val mediator: PhotoMediator,
    private val executor: Executor
) : ViewModel() {

    private fun getPhotosFromDb(): List<PexelPhotoItem> {
        val photos = photoStore.getCuratedPhotos(specifier = SizeSpecifier.LARGE)
        return photos.map(ToViewItem)
    }

    private fun queryCache(onData: (List<PexelPhotoItem>) -> Unit) {
        executor.execute {
            val cachedPhotos = getPhotosFromDb()
            onData(cachedPhotos)
        }
    }

    var pageSize: Int = 5
        set(value) {
            require(value >= 0)
            field = value
        }

    private val exposed: MutableLiveData<CuratedPhotosState> by lazy(LazyThreadSafetyMode.NONE) {
        val stateLiveData = MutableLiveData(
            CuratedPhotosState(
                loadedPhotos = emptyList(),
                showingFreshData = false,
                error = null,
                currentPage = 0,
                hasMore = false,
                status = CuratedPhotosState.Status.LOADING
            )
        )

        queryCache {
            stateLiveData.postValue(
                CuratedPhotosState(
                    loadedPhotos = it,
                    showingFreshData = false,
                    error = null,
                    currentPage = 0,
                    hasMore = false,
                    status = CuratedPhotosState.Status.IDLE
                )
            )
        }

        stateLiveData.observeForever { state ->
            Log.d(TAG, "New state: ${state.compactPrint()}")
            if (state.status == CuratedPhotosState.Status.IDLE
                && !state.showingFreshData
                && state.loadedPhotos.isEmpty()
            ) {
                // load fresh data if cache is empty
                refresh()
            }
        }

        stateLiveData
    }

    fun observePhotos(): LiveData<ViewState> {
        return exposed.map { state ->
            ViewState(
                photos = state.loadedPhotos,
                loadingMore = state.status == CuratedPhotosState.Status.LOADING,
                errorMsg = state.error
            )
        }
    }

    fun loadMore() {
        Log.d(TAG, "load more dispatched")
        val lastState = exposed.value ?: return

        if (lastState.status == CuratedPhotosState.Status.LOADING) {
            // already doing something
            return
        }

        if (!lastState.showingFreshData) {
            // has to try refresh
            refresh()
        } else if (lastState.hasMore) {
            // already paging
            val nextPage = lastState.currentPage + 1
            val transientState = lastState.copy(
                status = CuratedPhotosState.Status.LOADING
            )
            exposed.value = transientState
            mediator.performAction(
                PhotoMediator.Action.LoadPage(nextPage, pageSize)
            ) { result ->
                handleResult(result, transientState)
            }
        } else {
            // unhandled state
        }
    }

    fun refresh() {
        Log.d(TAG, "refresh dispatched")
        val lastState = exposed.value ?: return

        if (lastState.status == CuratedPhotosState.Status.LOADING) {
            // already doing something
            return
        }

        val transientState = lastState.copy(
            status = CuratedPhotosState.Status.LOADING
        )
        exposed.value = transientState
        mediator.performAction(PhotoMediator.Action.Refresh(pageSize = pageSize)) { result ->
            handleResult(result, transientState)
        }
    }

    private fun handleResult(result: PhotoMediator.Result, lastState: CuratedPhotosState) {
        Log.d(TAG, "result: $result, last state: ${lastState.compactPrint()}")
        val newState = when (result) {
            is PhotoMediator.Result.Failure -> {
                lastState.copy(
                    showingFreshData = false,
                    error = result.msg,
                    hasMore = false,
                    status = CuratedPhotosState.Status.IDLE
                )
            }
            is PhotoMediator.Result.Success -> {
                val newPhotos = getPhotosFromDb()
                when (val action = result.action) {
                    is PhotoMediator.Action.LoadPage -> lastState.copy(
                        loadedPhotos = newPhotos,
                        showingFreshData = true,
                        error = null,
                        currentPage = action.page,
                        hasMore = newPhotos.size >= lastState.loadedPhotos.size + action.pageSize,
                        status = CuratedPhotosState.Status.IDLE
                    )
                    is PhotoMediator.Action.Refresh -> lastState.copy(
                        loadedPhotos = newPhotos,
                        showingFreshData = true,
                        error = null,
                        currentPage = 1,
                        hasMore = true,
                        status = CuratedPhotosState.Status.IDLE
                    )
                }
            }
        }

        exposed.postValue(newState)
    }

    data class ViewState(
        val photos: List<PexelPhotoItem>,
        val loadingMore: Boolean,
        val errorMsg: String? = null
    )

    object Factory : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass == CuratedPhotosViewModel::class.java)
            val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PexelTestApp
            val deps = app.dependenciesContainer
            return CuratedPhotosViewModel(
                photoStore = deps.pexelPhotoStore,
                mediator = deps.photoMediator,
                executor = deps.ioExecutor
            ) as T
        }
    }
}

private fun CuratedPhotosState.compactPrint(): String {
    return buildString {
        append('{')
        append("showingFreshData: $showingFreshData, ")
        error?.let {
            append("error: $it, ")
        }
        append("currentPage: $currentPage, ")
        append("hasMore: $hasMore, ")
        append("status: $status, ")
        loadedPhotos.joinTo(
            buffer = this,
            prefix = "photos: [",
            postfix = "]",
            transform = { it.id.toString() }
        )
        append('}')
    }
}

object ToViewItem : (DbPhotoWithUrl) -> PexelPhotoItem {
    override fun invoke(dbPhoto: DbPhotoWithUrl): PexelPhotoItem = PexelPhotoItem(
        id = dbPhoto.id,
        photographerName = dbPhoto.authorName,
        src = dbPhoto.imageUrl,
        averageColor = dbPhoto.averageColor
    )
}

private data class CuratedPhotosState(
    val loadedPhotos: List<PexelPhotoItem>,
    val showingFreshData: Boolean,
    val error: String? = null,
    val currentPage: Int,
    val hasMore: Boolean,
    val status: Status
) {

    enum class Status {
        IDLE,
        LOADING
    }
}
