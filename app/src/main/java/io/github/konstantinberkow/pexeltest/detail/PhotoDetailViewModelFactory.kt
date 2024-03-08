package io.github.konstantinberkow.pexeltest.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.konstantinberkow.pexeltest.app.GlobalDependencies

object PhotoDetailViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass == PhotoDetailViewModel::class.java)

        return PhotoDetailViewModel(
            pexelApiProvider = { GlobalDependencies.pexelApi }
        ) as T
    }
}
