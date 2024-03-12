package io.github.konstantinberkow.pexeltest.curated

import io.github.konstantinberkow.pexeltest.util.BindableItem
import io.github.konstantinberkow.pexeltest.util.ListBasedDiffUtilCallback

private typealias FeedItems = List<CuratedPhotoFeedItem>

sealed interface CuratedPhotoFeedItem : BindableItem {

    class Photo(val pexelPhotoItem: PexelPhotoItem) : CuratedPhotoFeedItem {

        override val id: Long
            get() = pexelPhotoItem.id

        override val type: Int = Types.photo
    }

    class LoaderPlaceholder(override val id: Long) : CuratedPhotoFeedItem {

        override val type: Int = Types.loader
    }

    object Types {
        const val photo: Int = 1
        const val loader: Int = 2
    }

    object DiffUtilFactoryCallbackFactory :
            (FeedItems, FeedItems) -> ListBasedDiffUtilCallback<CuratedPhotoFeedItem> {
        override fun invoke(
            oldItems: FeedItems,
            newItems: FeedItems
        ): ListBasedDiffUtilCallback<CuratedPhotoFeedItem> {
            return DiffCallback(oldItems, newItems)
        }
    }

    private class DiffCallback(
        oldItems: FeedItems,
        newItems: FeedItems
    ) : ListBasedDiffUtilCallback<CuratedPhotoFeedItem>(oldItems, newItems) {

        override fun areItemsTheSame(
            oldItem: CuratedPhotoFeedItem,
            newItem: CuratedPhotoFeedItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CuratedPhotoFeedItem,
            newItem: CuratedPhotoFeedItem
        ): Boolean {
            return when (oldItem) {
                is LoaderPlaceholder -> true
                is Photo -> {
                    newItem as Photo
                    oldItem.pexelPhotoItem.photographerName == newItem.pexelPhotoItem.photographerName
                }
            }
        }
    }
}
