package com.framwork.useraddress.lib.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * EndlessRecyclerViewScrollListener helper class is used to manage pagination in RecyclerView.
 *
 * @author Aditi Shirsat
 */
abstract class EndlessRecyclerViewScrollListener : RecyclerView.OnScrollListener {

    /**
     * The minimum amount of items to have below your current scroll position before loading more.
     */
    private var mVisibleThreshold = 5
    /**
     * The current offset index of data you have loaded.
     */
    private var currentPage = 1

    /**
     * The total number of items in the dataSet after the last load
     */
    private var previousTotalItemCount = 0

    /**
     * True if we are still waiting for the last set of data to load.
     */
    private var loading = true

    /**
     * Sets the starting page index
     */
    private var mStartingPageIndex = 0

    private var mLayoutManager: RecyclerView.LayoutManager? = null

    fun setInitialValues() {
        mVisibleThreshold = 5
        currentPage = 1
        previousTotalItemCount = 0
        loading = true
        mStartingPageIndex = 0
    }

    constructor(linearLayoutManager: LinearLayoutManager) {
        init()
        this.mLayoutManager = linearLayoutManager
    }

    constructor(gridLayoutManager: GridLayoutManager) {
        init()
        this.mLayoutManager = gridLayoutManager
    }

    constructor(staggeredGridLayoutManager: StaggeredGridLayoutManager) {
        init()
        this.mLayoutManager = staggeredGridLayoutManager
    }


    private fun init() {
        mStartingPageIndex = getStartingPageIndex()

        val threshold = getVisibleThreshold()
        if (threshold > mVisibleThreshold) {
            mVisibleThreshold = threshold
        }
    }

    /*
     This happens many times a second during a scroll, so be wary of the code you place here.
     We are given a few useful parameters to help us work out if we need to load some more data,
     but first we check if we are waiting for the previous load to finish.
     */
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        //  when dy=0---->list is clear totalItemCount == 0 or init load  previousTotalItemCount=0
        if (dy <= 0) return

        val adapter = recyclerView.adapter
        val totalItemCount = adapter!!.itemCount

        val lastVisibleItemPosition = getLastVisibleItemPosition()

        val isAllowLoadMore = lastVisibleItemPosition + mVisibleThreshold > totalItemCount

        if (isAllowLoadMore) {

            if (totalItemCount > previousTotalItemCount) loading = false

            if (!loading) {
                /*
                 If it isn’t currently loading, we check to see if we have breached
                 the visibleThreshold and need to reload more data.
                 If we do need to reload some more data, we execute onLoadMore to fetch the data.
                 threshold should reflect how many total columns there are too
                 */
                previousTotalItemCount = totalItemCount
                currentPage++
                onLoadMore(currentPage, totalItemCount)
                loading = true
            }
        }
    }

    /**
     * @return Last visible item position
     */
    private fun getLastVisibleItemPosition(): Int {
        var lastVisibleItemPosition = 0

        when (mLayoutManager) {
            is StaggeredGridLayoutManager -> {
                val lastVisibleItemPositions =
                    (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
                // get maximum element within the list
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
            }
            is LinearLayoutManager -> {
                lastVisibleItemPosition =
                    (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                sendLastVisibleItemPosition(
                    (mLayoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition(),
                    mVisibleThreshold
                )
            }
            is GridLayoutManager -> lastVisibleItemPosition =
                (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
        }
        return lastVisibleItemPosition
    }

    /**
     * @return Last visible item
     */
    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    /**
     * @return visible Threshold default: 5
     */
    private fun getVisibleThreshold(): Int {
        return mVisibleThreshold
    }

    /**
     * @return Starting Page Index default: 0
     */
    private fun getStartingPageIndex(): Int {
        return mStartingPageIndex
    }

    //Defines the process for actually loading more data based on page
    abstract fun onLoadMore(page: Int, totalItemsCount: Int)

    //Sends last visible item position
    protected abstract fun sendLastVisibleItemPosition(
        lastCompletelyVisibleItemPosition: Int,
        lastVisibleItemPosition: Int
    )
}