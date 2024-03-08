package io.github.konstantinberkow.pexeltest.curated

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import io.github.konstantinberkow.pexeltest.R
import io.github.konstantinberkow.pexeltest.util.LoadMoreScrollListener

private const val TAG = "CuratedPhotosFragment"

class CuratedPhotosFragment : Fragment() {

    private lateinit var swipeRefreshView: SwipeRefreshLayout

    private lateinit var photosRecyclerView: RecyclerView

    private var loadMoreScrollListener: LoadMoreScrollListener? = null

    private var adapter: PhotosAdapter? = null

    private lateinit var viewModel: CuratedPhotosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PhotosAdapter(
            onPhotoClicked = ::navigate,
            imageLoader = Glide.with(this)
        )

        this.viewModel = ViewModelProvider(this, CuratedPhotosViewModelFactory)
            .get(CuratedPhotosViewModel::class.java)
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

        swipeRefreshView = layout.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_view).also {
            it.setOnRefreshListener {
                viewModel.refresh()
            }
        }

        photosRecyclerView =
            layout.findViewById<RecyclerView>(R.id.photos_recycler_view).also { recyclerView ->
                val layoutManager = LinearLayoutManager(
                    requireActivity(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapter

                loadMoreScrollListener = LoadMoreScrollListener(
                    layoutManager = layoutManager,
                    loadThreshold = 5,
                    loadMore = { viewModel.loadMore() }
                ).also {
                    recyclerView.addOnScrollListener(it)
                }
            }

        return layout
    }

    override fun onStart() {
        super.onStart()

        val recyclerAdapter = adapter ?: return
        val loadMoreListener = loadMoreScrollListener ?: return

        viewModel.observePhotos().observe(this) { viewState ->
            if (viewState.loadingMore && viewState.photos.isEmpty()) {
                swipeRefreshView.isRefreshing = true
            }

            if (!viewState.loadingMore) {
                loadMoreListener.notifyLoadCompleted()
                swipeRefreshView.isRefreshing = false
            }

            val newPhotos = viewState.photos
            recyclerAdapter.submitPhotos(newPhotos)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadMoreScrollListener = null
        adapter = null
    }
}