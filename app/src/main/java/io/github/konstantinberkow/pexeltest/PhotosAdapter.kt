package io.github.konstantinberkow.pexeltest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.github.konstantinberkow.pexeltest.data.PexelPhotoItem
import java.io.Closeable

private const val TAG = "PhotosAdapter"

class PhotosAdapter(
    private val onPhotoClicked: (PexelPhotoItem) -> Unit,
    private val loadImage: (String, ImageView) -> Closeable
) : RecyclerView.Adapter<PhotoItemViewHolder>() {

    private var photos: MutableList<PexelPhotoItem> = mutableListOf()

    private var cachedInflater: LayoutInflater? = null

    init {
        setHasStableIds(true)
    }

    private fun getLayoutInflater(parent: ViewGroup): LayoutInflater {
        val oldInflater = cachedInflater
        if (oldInflater != null) {
            return oldInflater
        }

        return LayoutInflater.from(parent.context).also {
            cachedInflater = it
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemViewHolder {
        val inflater = getLayoutInflater(parent)
        val itemView = inflater.inflate(R.layout.photo_item_view, parent, false)
        return PhotoItemViewHolder(itemView, onPhotoClicked, loadImage)
    }

    override fun onBindViewHolder(holder: PhotoItemViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun onViewRecycled(holder: PhotoItemViewHolder) {
        holder.unbind()
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun getItemId(position: Int): Long {
        return photos[position].id
    }

    fun replacePhotos(newPhotos: List<PexelPhotoItem>) {
        this.photos.clear()
        this.photos.addAll(newPhotos)
        this.notifyDataSetChanged()
    }

    fun addPhotos(newPhotos: List<PexelPhotoItem>) {
        if (newPhotos.isNotEmpty()) {
            val oldListSize = this.photos.size
            this.photos.addAll(newPhotos)
            this.notifyItemRangeInserted(oldListSize, newPhotos.size)
        }
    }
}
