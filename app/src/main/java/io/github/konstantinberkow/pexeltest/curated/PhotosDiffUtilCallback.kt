package io.github.konstantinberkow.pexeltest.curated

import io.github.konstantinberkow.pexeltest.util.ListBasedDiffUtilCallback

class PhotosDiffUtilCallback(
    oldList: List<PexelPhotoItem>,
    newList: List<PexelPhotoItem>,
) : ListBasedDiffUtilCallback<PexelPhotoItem>(oldList, newList) {

    override fun areItemsTheSame(oldItem: PexelPhotoItem, newItem: PexelPhotoItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PexelPhotoItem, newItem: PexelPhotoItem): Boolean {
        return oldItem.photographerName == newItem.photographerName
    }
}
