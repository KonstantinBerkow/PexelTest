package io.github.konstantinberkow.pexeltest.curated

import androidx.recyclerview.widget.DiffUtil
import io.github.konstantinberkow.pexeltest.util.BindableItem

sealed interface CuratedPhotoFeedItem : BindableItem {

    class Photo(val pexelPhotoItem: PexelPhotoItem) : CuratedPhotoFeedItem {

        override val id: Long
            get() = pexelPhotoItem.id

        override val type: Int = Types.PHOTO
    }

    class LoaderPlaceholder(override val id: Long) : CuratedPhotoFeedItem {

        override val type: Int = Types.LOADER
    }

    object Types {
        const val PHOTO: Int = 1
        const val LOADER: Int = 2
    }

    object ItemCallbackFactory : DiffUtil.ItemCallback<CuratedPhotoFeedItem>() {

        override fun areItemsTheSame(
            oldItem: CuratedPhotoFeedItem,
            newItem: CuratedPhotoFeedItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CuratedPhotoFeedItem,
            newItem: CuratedPhotoFeedItem
        ): Boolean {
            return when (oldItem) {
                is LoaderPlaceholder -> true
                is Photo -> {
                    newItem as Photo
                    oldItem.pexelPhotoItem.photographerName == newItem.pexelPhotoItem.photographerName
                }
            }
        }
    }
}
