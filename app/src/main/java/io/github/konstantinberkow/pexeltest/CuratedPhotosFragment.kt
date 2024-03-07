package io.github.konstantinberkow.pexeltest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.konstantinberkow.pexeltest.data.PexelPhotoItem
import java.io.Closeable

private const val TAG = "CuratedPhotosFragment"

class CuratedPhotosFragment : Fragment() {

    private var photosRecyclerView: RecyclerView? = null

    private var adapter: PhotosAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PhotosAdapter(
            onPhotoClicked = ::navigate,
            loadImage = ::loadImage
        )
    }

    private fun navigate(photo: PexelPhotoItem) {
        Log.d(TAG, "Open detail page: $photo")
    }

    private fun loadImage(url: String, into: ImageView): Closeable {
        Log.d(TAG, "load image: $url")
        into.setImageResource(R.mipmap.ic_launcher)
        return Closeables.EMPTY
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
