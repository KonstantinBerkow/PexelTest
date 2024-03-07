package io.github.konstantinberkow.pexeltest

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

    protected fun getOldItemAt(position: Int): T = oldList[position]

    protected fun getNewItemAt(position: Int): T = newList[position]
}
