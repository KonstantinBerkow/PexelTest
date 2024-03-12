package io.github.konstantinberkow.pexeltest.util

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections
import java.util.IdentityHashMap

class MixedTypesAdapter<T : BindableItem>(
    private val viewHolderConfigs: SparseArray<ViewHolderConfig<T>>,
    private val detectMoves: Boolean,
    private val diffUtilCallbackFactory: (List<T>, List<T>) -> DiffUtil.Callback
) : RecyclerView.Adapter<BindableViewHolder<T>>() {

    private val cachedInflaters: MutableMap<RecyclerView, LayoutInflater> = IdentityHashMap()

    private var currentItems: List<T> = emptyList()

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        return currentItems.size
    }

    override fun getItemId(position: Int): Long {
        return currentItems[position].id
    }

    override fun getItemViewType(position: Int): Int {
        return currentItems[position].type
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        cachedInflaters[recyclerView] = LayoutInflater.from(recyclerView.context)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        cachedInflaters.remove(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<T> {
        val inflater = cachedInflaters[parent]!!
        val config = viewHolderConfigs.get(viewType)
        val itemView = inflater.inflate(config.layoutId, parent, false)
        return config.holderFactory(itemView)
    }

    override fun onBindViewHolder(holder: BindableViewHolder<T>, position: Int) {
        val item = currentItems[position]
        holder.bind(item, Collections.emptyList())
    }

    override fun onBindViewHolder(
        holder: BindableViewHolder<T>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = currentItems[position]
        holder.bind(item, payloads)
    }

    override fun onViewRecycled(holder: BindableViewHolder<T>) {
        holder.recycle()
    }

    fun submitList(newItems: List<T>) {
        val oldItems = currentItems
        val callback = diffUtilCallbackFactory(oldItems, newItems)
        val diff = DiffUtil.calculateDiff(callback, detectMoves)
        currentItems = newItems
        diff.dispatchUpdatesTo(this)
    }
}

data class ViewHolderConfig<T : BindableItem>(
    val layoutId: Int,
    val holderFactory: (View) -> BindableViewHolder<T>
)

interface BindableItem {
    val id: Long
    val type: Int
}
