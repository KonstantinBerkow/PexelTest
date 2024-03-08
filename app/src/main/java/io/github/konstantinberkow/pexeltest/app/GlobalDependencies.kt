package io.github.konstantinberkow.pexeltest.app

import android.graphics.Color
import android.util.Log
import com.google.gson.GsonBuilder
import io.github.konstantinberkow.pexeltest.BuildConfig
import io.github.konstantinberkow.pexeltest.network.PexelApi
import io.github.konstantinberkow.pexeltest.network.PexelPhoto
import io.github.konstantinberkow.pexeltest.network.PexelPhotoGsonAdapter
import io.github.konstantinberkow.pexeltest.network.PexelPhotoPage
import io.github.konstantinberkow.pexeltest.network.PexelPhotoPageGsonAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.pexels.com/v1/"

private const val TAG = "GlobalDependencies"

object GlobalDependencies {

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
        val okhttp = OkHttpClient.Builder()
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

        val retrofitInstance = Retrofit.Builder()
            .client(okhttp)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .validateEagerly(true)
            .build()

        Log.d(TAG, "Create api instance")
        retrofitInstance.create(PexelApi::class.java)
    }
}
