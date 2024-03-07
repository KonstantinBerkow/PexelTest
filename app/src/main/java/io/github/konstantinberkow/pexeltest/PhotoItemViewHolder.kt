package io.github.konstantinberkow.pexeltest

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.github.konstantinberkow.pexeltest.data.PexelPhotoItem
import java.io.Closeable

class PhotoItemViewHolder(
    itemView: View,
    onPhotoClicked: (PexelPhotoItem) -> Unit,
    private val loadImage: (String, ImageView) -> Closeable
) : RecyclerView.ViewHolder(itemView) {

    private val cardView: CardView
    private val photoImageView: ImageView
    private val authorTextView: TextView

    private var photo: PexelPhotoItem? = null

    private var latestImageRequest: Closeable? = null

    init {
        cardView = itemView as CardView
        photoImageView = itemView.findViewById(R.id.photo_image_view)
        authorTextView = itemView.findViewById(R.id.author_text_view)

        itemView.setOnClickListener {
            photo?.let { onPhotoClicked(it) }
        }
    }

    fun bind(photo: PexelPhotoItem) {
        this.photo = photo

        cardView.setCardBackgroundColor(Color.parseColor(photo.averageColor))
        authorTextView.text = photo.photographerName

        latestImageRequest?.close()
        latestImageRequest = loadImage(photo.srcSmall, photoImageView)
    }

    fun unbind() {
        photo = null

        latestImageRequest?.close()
        latestImageRequest = null
    }
}
