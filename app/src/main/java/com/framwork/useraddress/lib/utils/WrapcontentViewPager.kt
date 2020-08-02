package com.framwork.useraddress.lib.utils

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

/**
 * WrapContentViewPager
 *
 * @author Aditi Shirsat
 */

class WrapContentViewPager : ViewPager {
    private var mCurrentPagePosition = 0

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context,attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        try {
            val wrapHeight =
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST
            if (wrapHeight) {
                val child = getChildAt(mCurrentPagePosition)
                if (child != null) {
                    child.measure(
                        widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    )
                    val h = child.measuredHeight
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun reMeasureCurrentPage(position: Int) {
        mCurrentPagePosition = position
        requestLayout()
    }
}