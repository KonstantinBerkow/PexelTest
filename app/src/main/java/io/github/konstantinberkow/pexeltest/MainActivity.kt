package io.github.konstantinberkow.pexeltest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate, savedInstanceState: $savedInstanceState")

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        if (savedInstanceState == null) {
            Log.d(TAG, "create new fragment")
            val fragment = CuratedPhotosFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.content_frame, fragment, Screens.CURATED_PHOTOS.name)
                .commitAllowingStateLoss()
        } else {
            Log.d(
                TAG,
                "old top fragment: ${supportFragmentManager.findFragmentById(R.id.content_frame)}"
            )
        }
    }

    private enum class Screens {
        CURATED_PHOTOS
    }
}
