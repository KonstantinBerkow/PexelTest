package io.github.konstantinberkow.pexeltest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.konstantinberkow.pexeltest.data.PexelPhotoItem

private const val TAG = "CuratedPhotosFragment"

class CuratedPhotosFragment : Fragment() {

    private var photosRecyclerView: RecyclerView? = null

    private var adapter: PhotosAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PhotosAdapter(
            onPhotoClicked = ::navigate,
            imageLoader = Glide.with(this)
        )
    }

    private fun navigate(photo: PexelPhotoItem) {
        Log.d(TAG, "Open detail page: $photo")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_curated_photos, container, false)

        layout.findViewById<RecyclerView>(R.id.photos_recycler_view).also { recyclerView ->
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            recyclerView.adapter = adapter

            photosRecyclerView = recyclerView
        }

        return layout
    }

    override fun onResume() {
        super.onResume()

        adapter?.replacePhotos(
            PexelPhotoItem.MOCK_ITEMS
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
    }
}
