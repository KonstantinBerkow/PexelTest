package io.github.konstantinberkow.pexeltest.app

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.gson.GsonBuilder
import io.github.konstantinberkow.pexeltest.BuildConfig
import io.github.konstantinberkow.pexeltest.cache.DbPexelPhotoStore
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

    val pexelPhotoStore by lazy {
        Log.d(TAG, "Create pexelPhotoStore")
        DbPexelPhotoStore(
            context = appContext,
            photoBasePath = "https://images.pexels.com/photos/"
        )
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
            executor = ioExecutor
        )
    }
}
