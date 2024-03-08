package io.github.konstantinberkow.pexeltest.curated

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.GlobalDependencies

private const val TAG = "CuratedPhotosViewModelFactory"

object CuratedPhotosViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass == CuratedPhotosViewModel::class.java)
        val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
//        val savedStateHandle = extras.createSavedStateHandle()
        Log.d(TAG, "App instance: $app")
//        Log.d(TAG, "Saved state handle: $savedStateHandle")
        return CuratedPhotosViewModel(
            pexelApiProvider = { GlobalDependencies.pexelApi }
        ) as T
    }
}
