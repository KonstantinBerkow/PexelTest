package io.github.konstantinberkow.pexeltest.curated

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.konstantinberkow.pexeltest.R

//class PhotosAdapter(
//    private val onPhotoClicked: (PexelPhotoItem) -> Unit,
//    private val imageLoader: RequestManager
//) : RecyclerView.Adapter<PhotoItemViewHolder>() {
//
//    private var photos: List<PexelPhotoItem> = emptyList()
//
//    private var cachedInflater: LayoutInflater? = null
//
//    init {
//        setHasStableIds(true)
//    }
//
//    private fun getLayoutInflater(parent: ViewGroup): LayoutInflater {
//        val oldInflater = cachedInflater
//        if (oldInflater != null) {
//            return oldInflater
//        }
//
//        return LayoutInflater.from(parent.context).also {
//            cachedInflater = it
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemViewHolder {
//        val inflater = getLayoutInflater(parent)
//        val itemView = inflater.inflate(R.layout.photo_item_view, parent, false)
//        return PhotoItemViewHolder(itemView, onPhotoClicked, imageLoader)
//    }
//
//    override fun onBindViewHolder(holder: PhotoItemViewHolder, position: Int) {
//        holder.bind(photos[position])
//    }
//
//    override fun onViewRecycled(holder: PhotoItemViewHolder) {
//        holder.unbind()
//    }
//
//    override fun getItemCount(): Int {
//        return photos.size
//    }
//
//    override fun getItemId(position: Int): Long {
//        return photos[position].id
//    }
//
//    fun submitPhotos(newPhotos: List<PexelPhotoItem>) {
//        val diff = DiffUtil.calculateDiff(PhotosDiffUtilCallback(photos, newPhotos), false)
//        photos = newPhotos
//        diff.dispatchUpdatesTo(this)
//    }
//}