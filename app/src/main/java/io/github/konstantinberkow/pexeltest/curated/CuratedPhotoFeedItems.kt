package io.github.konstantinberkow.pexeltest.curated

import androidx.recyclerview.widget.DiffUtil
import io.github.konstantinberkow.pexeltest.util.BindableItem

object CuratedPhotoFeedItems {

    const val PHOTO: Int = 1

    const val LOADER: Int = 2

    fun wrapPhoto(internal: PexelPhotoItem) =
        BindableItem(id = internal.id, type = PHOTO, data = internal)

    fun wrapLoader(id: Long) =
        BindableItem(id = id, type = LOADER, data = Unit)

    object ItemCallbackFactory : DiffUtil.ItemCallback<BindableItem<out Any>>() {

        override fun areItemsTheSame(
            oldItem: BindableItem<out Any>,
            newItem: BindableItem<out Any>
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: BindableItem<out Any>,
            newItem: BindableItem<out Any>
        ): Boolean {
            if (oldItem.type == LOADER) {
                return true
            }

            val oldData = oldItem.data
            val newData = newItem.data
            oldData as PexelPhotoItem
            newData as PexelPhotoItem
            return oldData.photographerName == newData.photographerName
        }
    }
}
