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

class MixedTypesAdapter(
    private val viewHolderConfigs: SparseArray<ViewHolderConfig>,
    diffConfig: AsyncDifferConfig<BindableItem<out Any>>
) : ListAdapter<BindableItem<out Any>, BindableViewHolder<Any>>(diffConfig) {

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

    private fun getLayoutInflater(parent: ViewGroup): LayoutInflater {
        return LayoutInflater.from(parent.context)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        cachedInflaters[recyclerView] = getLayoutInflater(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        cachedInflaters.remove(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<Any> {
        val inflater = cachedInflaters[parent] ?: getLayoutInflater(parent)
        val config = viewHolderConfigs.get(viewType)
        val itemView = inflater.inflate(config.layoutId, parent, false)
        return config.holderFactory(itemView)
    }

    override fun onBindViewHolder(holder: BindableViewHolder<Any>, position: Int) {
        val item = getItem(position)
        holder.bind(item.data, Collections.emptyList())
    }

    override fun onBindViewHolder(
        holder: BindableViewHolder<Any>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItem(position)
        holder.bind(item.data, payloads)
    }

    override fun onViewRecycled(holder: BindableViewHolder<Any>) {
        holder.recycle()
    }
}

data class ViewHolderConfig(
    val layoutId: Int,
    val holderFactory: (View) -> BindableViewHolder<Any>,
)

data class BindableItem<T>(
    val id: Long,
    val type: Int,
    val data: T
)
