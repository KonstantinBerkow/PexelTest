package io.github.konstantinberkow.pexeltest.curated

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.RequestManager
import io.github.konstantinberkow.pexeltest.R
import io.github.konstantinberkow.pexeltest.util.BindableViewHolder

class PhotoItemViewHolder(
    itemView: View,
    onPhotoClicked: (PexelPhotoItem) -> Unit,
    private val imageLoader: RequestManager
) : BindableViewHolder<PexelPhotoItem>(itemView) {

    private val cardView: CardView
    private val photoImageView: ImageView
    private val authorTextView: TextView

    init {
        cardView = itemView as CardView
        photoImageView = itemView.findViewById(R.id.photo_image_view)
        authorTextView = itemView.findViewById(R.id.author_text_view)

        itemView.setOnClickListener {
            withData {
                onPhotoClicked(it)
            }
        }
    }

    override fun onBind(data: PexelPhotoItem, payloads: List<Any?>) {
        cardView.setCardBackgroundColor(data.averageColor)

        authorTextView.text = data.photographerName
        authorTextView.setBackgroundColor(inverseColor(data.averageColor))

        imageLoader.clear(photoImageView)

        imageLoader.load(data.src)
            .placeholder(R.mipmap.ic_launcher)
            .into(photoImageView)
    }

    override fun onRecycled() {
        imageLoader.clear(photoImageView)
    }

    private fun inverseColor(averageColor: Int): Int {
        val red = Color.red(averageColor)
        val green = Color.green(averageColor)
        val blue = Color.blue(averageColor)
        return Color.rgb(255 - red, 255 - green, 255 - blue)
    }
}
