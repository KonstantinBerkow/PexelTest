package io.github.konstantinberkow.pexeltest.util

import androidx.recyclerview.widget.DiffUtil

abstract class ListBasedDiffUtilCallback<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    final override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    final override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    final override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return getChangePayload(oldList[oldItemPosition], newList[newItemPosition])
    }

    abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    fun getChangePayload(oldItem: T, newItem: T): Any? {
        return null
    }
}
