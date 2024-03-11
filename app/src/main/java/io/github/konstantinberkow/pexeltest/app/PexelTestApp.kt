package io.github.konstantinberkow.pexeltest.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class PexelTestApp : Application() {

    val dependenciesContainer by lazy { AppDependenciesContainer(this) }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
