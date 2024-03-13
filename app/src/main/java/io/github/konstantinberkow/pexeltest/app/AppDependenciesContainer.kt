package io.github.konstantinberkow.pexeltest.app

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.google.gson.GsonBuilder
import io.github.konstantinberkow.pexeltest.BuildConfig
import io.github.konstantinberkow.pexeltest.Database
import io.github.konstantinberkow.pexeltest.cache.DbPexelPhotoStore
import io.github.konstantinberkow.pexeltest.cache.PexelPhotoStoreLoggingProxy
import io.github.konstantinberkow.pexeltest.data.CombinedPhotoMediator
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import io.github.konstantinberkow.pexeltest.network.PexelPhotoGsonAdapter
import io.github.konstantinberkow.pexeltest.network.PexelPhotoPage
import io.github.konstantinberkow.pexeltest.network.PexelPhotoPageGsonAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "AppDependenciesContainer"

class AppDependenciesContainer(
    private val appContext: Context
) {

    private fun pexelPhotImageUrlDbAdapter(): DbPexelPhotoStore.ImageUrlSaveContract {
        return object : DbPexelPhotoStore.ImageUrlSaveContract {

            private val baseUri = Uri.parse("https://images.pexels.com/photos")

            override fun extractPartToSave(url: String): String {
                return Uri.parse(url).query ?: ""
            }

            override fun restoreImageUrl(photoId: Long, savedPart: String): String {
                // photoBasePath + "/:id/pexels-photo-:id.jpeg?query=...
                val strId = photoId.toString()
                val fullUrl = baseUri.buildUpon()
                    .appendPath(strId)
                    .appendPath("pexels-photo-$strId.jpeg")
                    .encodedQuery(savedPart)
                    .build()
                return fullUrl.toString()
            }
        }
    }

    val pexelPhotoStore by lazy {
        Log.d(TAG, "Create pexelPhotoStore")
        DbPexelPhotoStore(
            databaseProvider = {
                val driver = AndroidSqliteDriver(
                    schema = Database.Schema,
                    context = appContext,
                    name = "pexel_photos.db"
                )
                Database(driver)
            },
            imageUrlSaveAdapter = pexelPhotImageUrlDbAdapter()
        ).let {
            PexelPhotoStoreLoggingProxy(it)
        }
    }

    val gson by lazy {
        Log.d(TAG, "Create gson")
        val photoAdapter = PexelPhotoGsonAdapter(parseColor = Color::parseColor)
        val photoPageAdapter = PexelPhotoPageGsonAdapter(photoAdapter)
        GsonBuilder()
            .registerTypeAdapter(PexelPhoto::class.java, photoAdapter)
            .registerTypeAdapter(PexelPhotoPage::class.java, photoPageAdapter)
            .create()
    }

    val pexelApi by lazy {
        Log.d(TAG, "Create okhttp instance")
        val okHttpClient = OkHttpClient.Builder()
            .also {
                if (BuildConfig.DEBUG) {
                    val loggingInterceptor = HttpLoggingInterceptor { message ->
                        Log.v("Retrofit", message)
                    }
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    it.addInterceptor(loggingInterceptor)
                }
            }
            .build()

        Log.d(TAG, "Create retrofit")
        val retrofitInstance = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.pexels.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .validateEagerly(true)
            .build()

        Log.d(TAG, "Create api instance")
        retrofitInstance.create(PexelApi::class.java)
    }

    val ioExecutor by lazy {
        Log.d(TAG, "Create IO executor")
        val threadCounter = AtomicInteger()
        Executors.newCachedThreadPool { work ->
            Thread(work, "App-IO-Thread-${threadCounter.incrementAndGet()}")
        }
    }

    val photoMediator by lazy {
        Log.d(TAG, "Create photoMediator")
        CombinedPhotoMediator(
            pexelPhotoStore = pexelPhotoStore,
            pexelApi = pexelApi,
        )
    }
}
