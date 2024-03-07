package io.github.konstantinberkow.pexeltest

import io.github.konstantinberkow.pexeltest.data.PexelPhotoItem

class PhotosDiffUtilCallback(
    oldList: List<PexelPhotoItem>,
    newList: List<PexelPhotoItem>,
) : ListBasedDiffUtilCallback<PexelPhotoItem>(oldList, newList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return getOldItemAt(oldItemPosition).id == getNewItemAt(newItemPosition).id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return getOldItemAt(oldItemPosition).photographerName == getNewItemAt(newItemPosition).photographerName
    }
}
