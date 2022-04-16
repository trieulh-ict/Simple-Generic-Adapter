package io.trieulh.simplegenericadapter.listener

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.trieulh.simplegenericadapter.utils.findFirstVisibleItemPosition

/**
 * Created by Trieulh on 05,August,2019
 */
class EndlessScrollListener(
    private val layoutManager: RecyclerView.LayoutManager,
    private var visibleThreshold: Int = 0, // The minimum amount of items to have below your current scroll position before loading more.
    private val includeEmptyState: Boolean,
    private val loadMoreObserver: LoadMoreObserver
) : RecyclerView.OnScrollListener() {
    private var currentPage = 0 // The current offset index of data you have loaded
    private var previousTotalItemCount = 0 // The total number of items in the data set after the last load
    private var loading = false // True if we are still waiting for the last set of data to load.
    private val startingPageIndex = 0 // Sets the starting page index

    init {
        when (layoutManager) {
            is GridLayoutManager -> visibleThreshold *= layoutManager.spanCount
            is StaggeredGridLayoutManager -> visibleThreshold *= layoutManager.spanCount
        }
        resetState()
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = layoutManager.itemCount
        val visibleItemCount = view.childCount

        if (isUserScrolled(view)) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if (isLoadingFinished(totalItemCount)) {
                loading = false
                loadMoreObserver.onLoadingStateChanged(loading)
            } else if (shouldStartLoading(firstVisibleItemPosition, totalItemCount, visibleItemCount)) {
                currentPage++
                loadMoreObserver.onLoadMore(currentPage)
                loading = true
                loadMoreObserver.onLoadingStateChanged(loading)
            }
        }

        this.previousTotalItemCount = totalItemCount
    }

    fun resetState() {
        loading = false
        previousTotalItemCount = if (includeEmptyState) 1 else 0
    }

    private fun isUserScrolled(view: RecyclerView) = view.scrollState != RecyclerView.SCROLL_STATE_IDLE

    private fun shouldStartLoading(
        firstVisibleItemPosition: Int,
        totalItemCount: Int,
        visibleItemCount: Int
    ) =
        !loading && ((totalItemCount - visibleItemCount) <= (firstVisibleItemPosition + visibleThreshold))

    private fun isLoadingFinished(totalItemCount: Int) =
        loading && totalItemCount > (previousTotalItemCount + 1) // + 1 for the loading holder
}

interface LoadMoreObserver {
    fun onLoadingStateChanged(loading: Boolean)
    fun onLoadMore(currentPage: Int)
}