package io.github.konstantinberkow.pexeltest.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BindableViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var boundData: T? = null

    fun bind(data: T, payloads: List<Any?>) {
        boundData = data
        onBind(data, payloads)
    }

    fun recycle() {
        boundData = null
        onRecycled()
    }

    protected fun withData(action: (T) -> Unit) {
        boundData?.let(action)
    }

    open fun onBind(data: T, payloads: List<Any?>) {
    }

    open fun onRecycled() {
    }
}
