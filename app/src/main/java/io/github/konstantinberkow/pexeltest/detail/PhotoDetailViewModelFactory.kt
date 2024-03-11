package io.github.konstantinberkow.pexeltest.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.PexelTestApp

object PhotoDetailViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass == PhotoDetailViewModel::class.java)
        val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PexelTestApp

        return PhotoDetailViewModel(
            pexelApiProvider = { app.dependenciesContainer.pexelApi }
        ) as T
    }
}
