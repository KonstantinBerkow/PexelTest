package io.github.konstantinberkow.pexeltest.util

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "LoadMoreScrollListener"

class LoadMoreScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val loadThreshold: Int,
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    private var loading: Boolean = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        Log.v(TAG, "onScrolled dy: $dy, loading: $loading")
        if (dy <= 0 || loading) {
            return
        }

        val lastItemPosition = layoutManager.findLastVisibleItemPosition()
        val totalItemsCount = layoutManager.itemCount

        Log.v(TAG, "lastItemPosition: $lastItemPosition, totalItemsCount: $totalItemsCount")
        if (lastItemPosition + loadThreshold >= totalItemsCount) {
            loading = true
            loadMore()
        }
    }

    fun notifyLoadCompleted() {
        loading = false
    }
}
