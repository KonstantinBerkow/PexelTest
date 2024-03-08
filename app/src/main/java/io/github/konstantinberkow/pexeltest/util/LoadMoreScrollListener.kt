package io.github.konstantinberkow.pexeltest.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LoadMoreScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val loadThreshold: Int,
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    private var loading: Boolean = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy <= 0 || loading) {
            return
        }

        val lastItemPosition = layoutManager.findLastVisibleItemPosition()
        val totalItemsCount = layoutManager.itemCount

        if (lastItemPosition + loadThreshold > totalItemsCount) {
            loading = true
            loadMore()
        }
    }

    fun notifyLoadCompleted() {
        loading = false
    }
}
