package io.github.konstantinberkow.pexeltest.curated

import io.github.konstantinberkow.pexeltest.util.ListBasedDiffUtilCallback

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
