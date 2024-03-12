package io.github.konstantinberkow.pexeltest.data

import android.util.Log
import io.github.konstantinberkow.pexeltest.cache.DbPhoto
import io.github.konstantinberkow.pexeltest.cache.PexelPhotoStore
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.Executor
import java.util.concurrent.TimeoutException

private const val TAG = "CombinedPhotoMediator"

class CombinedPhotoMediator(
    private val pexelPhotoStore: PexelPhotoStore,
    private val pexelApi: PexelApi,
    private val executor: Executor
) : PhotoMediator {

    override fun performAction(
        action: PhotoMediator.Action,
        callback: (PhotoMediator.Result) -> Unit
    ) {
        Log.d(TAG, "perform action: $action")
        executor.execute {
            val result = try {
                val response = when (action) {
                    is PhotoMediator.Action.LoadPage -> pexelApi.curatedPhotos(action.page, 15)
                    PhotoMediator.Action.Refresh -> pexelApi.curatedPhotos(1, 15)
                }.execute()
                val body = response.body()
                val error = response.errorBody()
                if (body != null) {
                    when (action) {
                        is PhotoMediator.Action.LoadPage -> {
                            body.photos.map(ToDbPhoto).also {
                                pexelPhotoStore.addPhotos(it)
                            }
                        }

                        PhotoMediator.Action.Refresh -> {
                            body.photos.map(ToDbPhoto).also {
                                pexelPhotoStore.replacePhotos(it)
                            }
                        }
                    }
                    PhotoMediator.Result.Success(action)
                } else if (error != null) {
                    PhotoMediator.Result.Failure(action, error.toString())
                } else {
                    PhotoMediator.Result.Failure(action, "Unexpected")
                }
            } catch (e: IOException) {
                PhotoMediator.Result.Failure(action, "IO exception")
            } catch (e: TimeoutException) {
                PhotoMediator.Result.Failure(action, "Timeout exception")
            } catch (e: SocketTimeoutException) {
                PhotoMediator.Result.Failure(action, "Timeout exception")
            }
            callback(result)
        }
    }
}

private object ToDbPhoto : (PexelPhoto) -> DbPhoto {
    override fun invoke(photo: PexelPhoto) = DbPhoto(
        id = photo.id,
        authorName = photo.photographer,
        averageColor = photo.averageColor,
        src = photo.src
    )
}
