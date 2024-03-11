package io.github.konstantinberkow.pexeltest.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.PexelTestApp

object CuratedPhotosViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass == CuratedPhotosViewModel::class.java)
        val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PexelTestApp
        return CuratedPhotosViewModel(
            pexelApiProvider = { app.dependenciesContainer.pexelApi }
        ) as T
    }
}
