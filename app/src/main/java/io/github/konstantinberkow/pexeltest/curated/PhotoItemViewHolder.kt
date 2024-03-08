package io.github.konstantinberkow.pexeltest.curated

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.konstantinberkow.pexeltest.R

class PhotoItemViewHolder(
    itemView: View,
    onPhotoClicked: (PexelPhotoItem) -> Unit,
    private val imageLoader: RequestManager
) : RecyclerView.ViewHolder(itemView) {

    private val cardView: CardView
    private val photoImageView: ImageView
    private val authorTextView: TextView

    private var photo: PexelPhotoItem? = null

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

        cardView.setCardBackgroundColor(photo.averageColor)

        authorTextView.text = photo.photographerName
        authorTextView.setBackgroundColor(inverseColor(photo.averageColor))

        imageLoader.clear(photoImageView)

        imageLoader.load(photo.srcSmall)
            .placeholder(R.mipmap.ic_launcher)
            .into(photoImageView)
    }

    private fun inverseColor(averageColor: Int): Int {
        val red = Color.red(averageColor)
        val green = Color.green(averageColor)
        val blue = Color.blue(averageColor)
        return Color.rgb(255 - red, 255 - green, 255 - blue)
    }

    fun unbind() {
        photo = null

        imageLoader.clear(photoImageView)
    }
}
