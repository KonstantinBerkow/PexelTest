package io.github.konstantinberkow.pexeltest.util

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections
import java.util.IdentityHashMap

class MixedTypesAdapter<T : BindableItem>(
    private val viewHolderConfigs: SparseArray<ViewHolderConfig<T>>,
    diffConfig: AsyncDifferConfig<T>
) : ListAdapter<T, BindableViewHolder<T>>(diffConfig) {

    private val cachedInflaters: MutableMap<RecyclerView, LayoutInflater> = IdentityHashMap()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
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
        val item = getItem(position)
        holder.bind(item, Collections.emptyList())
    }

    override fun onBindViewHolder(
        holder: BindableViewHolder<T>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItem(position)
        holder.bind(item, payloads)
    }

    override fun onViewRecycled(holder: BindableViewHolder<T>) {
        holder.recycle()
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
