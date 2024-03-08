package io.github.konstantinberkow.pexeltest.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import io.github.konstantinberkow.pexeltest.R

class PhotoDetailFragment : Fragment() {

    private lateinit var photoImageView: ImageView

    private lateinit var authorNameTextView: TextView

    private lateinit var loadProgressBar: ProgressBar

    private lateinit var viewModel: PhotoDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewModel = ViewModelProvider(this, PhotoDetailViewModelFactory)
            .get(PhotoDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_photo_detail, container, false)

        photoImageView = layout.findViewById(R.id.photo_image_view)
        authorNameTextView = layout.findViewById(R.id.author_name_text_view)
        loadProgressBar = layout.findViewById(R.id.load_progress_view)

        return layout
    }

    override fun onStart() {
        super.onStart()

        val photoId = arguments?.getLong(PHOTO_ID_KEY, 0L) ?: 0L
        if (photoId != 0L) {
            viewModel.getPhoto(photoId).observe(this) { state ->
                val photoInfo = state.photoDetail
                if (photoInfo != null) {
                    showPhoto(photoInfo)
                } else {
                    hideInfo()
                }

                loadProgressBar.visibility = if (state.loading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun showPhoto(photo: PhotoDetail) {
        Glide.with(this)
            .load(photo.originalImageUrl)
            .into(photoImageView)

        authorNameTextView.text = photo.authorName
        (authorNameTextView.parent as View).visibility = View.VISIBLE
    }

    private fun hideInfo() {
        (authorNameTextView.parent as View).visibility = View.GONE
    }

    companion object {

        const val PHOTO_ID_KEY = "photo_id"
    }
}
