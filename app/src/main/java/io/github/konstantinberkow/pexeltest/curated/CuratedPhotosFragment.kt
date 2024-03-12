package io.github.konstantinberkow.pexeltest.curated

import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import io.github.konstantinberkow.pexeltest.R
import io.github.konstantinberkow.pexeltest.detail.PhotoDetailFragment
import io.github.konstantinberkow.pexeltest.util.LoadMoreScrollListener
import io.github.konstantinberkow.pexeltest.util.MixedTypesAdapter
import io.github.konstantinberkow.pexeltest.util.ViewHolderConfig

private const val TAG = "CuratedPhotosFragment"

class CuratedPhotosFragment : Fragment() {

    private lateinit var swipeRefreshView: SwipeRefreshLayout

    private lateinit var photosRecyclerView: RecyclerView

    private var loadMoreScrollListener: LoadMoreScrollListener? = null

    private var adapter: MixedTypesAdapter<CuratedPhotoFeedItem>? = null

    private lateinit var viewModel: CuratedPhotosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onPhotoClicked = { photo: PexelPhotoItem ->
            findNavController()
                .navigate(
                    R.id.action_curated_photos_fragment_to_photo_detail_fragment,
                    bundleOf(PhotoDetailFragment.PHOTO_ID_KEY to photo.id)
                )
        }
        val glide = Glide.with(this)

        val configs = SparseArray<ViewHolderConfig<CuratedPhotoFeedItem>>().apply {
            put(
                CuratedPhotoFeedItem.Types.PHOTO,
                ViewHolderConfig(
                    layoutId = R.layout.photo_item_view,
                    holderFactory = { itemView ->
                        PhotoItemViewHolder(
                            itemView = itemView,
                            onPhotoClicked = onPhotoClicked,
                            imageLoader = glide
                        )
                    }
                ) as ViewHolderConfig<CuratedPhotoFeedItem>
            )
            put(
                CuratedPhotoFeedItem.Types.LOADER,
                ViewHolderConfig(
                    layoutId = R.layout.loader_item,
                    holderFactory = { LoaderItemViewHolder(it) }
                ) as ViewHolderConfig<CuratedPhotoFeedItem>
            )
        }

        adapter = MixedTypesAdapter(
            viewHolderConfigs = configs,
            diffConfig = AsyncDifferConfig.Builder(CuratedPhotoFeedItem.ItemCallbackFactory)
                .build()
        )

        viewModel = ViewModelProvider(this, CuratedPhotosViewModel.Factory)
            .get(CuratedPhotosViewModel::class.java)
            .also {
                it.pageSize = 5
            }
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
                    loadThreshold = 1,
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
            val newPhotos = viewState.photos

            if (viewState.loadingMore && newPhotos.isEmpty()) {
                swipeRefreshView.isRefreshing = true
            }

            if (!viewState.loadingMore) {
                loadMoreListener.notifyLoadCompleted()
                swipeRefreshView.isRefreshing = false
            }

            val newItems = buildList {
                newPhotos.forEach {
                    add(CuratedPhotoFeedItem.Photo(it))
                }
                if (viewState.loadingMore && newPhotos.isNotEmpty()) {
                    add(CuratedPhotoFeedItem.LoaderPlaceholder(0))
                }
            }

            recyclerAdapter.submitList(newItems)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadMoreScrollListener = null
        adapter = null
    }
}
