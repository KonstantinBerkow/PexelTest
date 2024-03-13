package io.github.konstantinberkow.pexeltest.data

import android.util.Log
import io.github.konstantinberkow.pexeltest.cache.DbPhoto
import io.github.konstantinberkow.pexeltest.cache.PexelPhotoStore
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

private const val TAG = "CombinedPhotoMediator"

class CombinedPhotoMediator(
    private val pexelPhotoStore: PexelPhotoStore,
    private val pexelApi: PexelApi
) : PhotoMediator {

    override suspend fun performAction(action: PhotoMediator.Action): PhotoMediator.Result {
        Log.d(TAG, "perform action: $action")
        val result = try {
            val body = when (action) {
                is PhotoMediator.Action.LoadPage ->
                    pexelApi.curatedPhotos(action.page, action.pageSize)

                is PhotoMediator.Action.Refresh ->
                    pexelApi.curatedPhotos(1, action.pageSize)
            }
            when (action) {
                is PhotoMediator.Action.LoadPage ->
                    body.photos.map(ToDbPhoto).also {
                        pexelPhotoStore.addPhotos(it)
                    }

                is PhotoMediator.Action.Refresh ->
                    body.photos.map(ToDbPhoto).also {
                        pexelPhotoStore.replacePhotos(it)
                    }
            }
            PhotoMediator.Result.Success(action)
        } catch (e: IOException) {
            PhotoMediator.Result.Failure(action, "IO exception")
        } catch (e: TimeoutException) {
            PhotoMediator.Result.Failure(action, "Timeout exception")
        } catch (e: SocketTimeoutException) {
            PhotoMediator.Result.Failure(action, "Timeout exception")
        } catch (e: HttpException) {
            PhotoMediator.Result.Failure(action, "HTTP: ${e.code()}")
        }
        return result
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
