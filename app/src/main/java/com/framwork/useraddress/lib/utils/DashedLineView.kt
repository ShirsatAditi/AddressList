package com.framwork.useraddress.lib.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * DashedLineView class is used to display a straight dotted line.
 *
 * @author Aditi Shirsat
 */
class DashedLineView : View {

    private var mPaint: Paint? = null
    private var mPath: Path? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mPaint = Paint()
        mPaint?.style = Paint.Style.STROKE
        mPaint?.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        mPath = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val vWidth = measuredWidth
        val vHeight = measuredHeight
        mPath?.moveTo(0F, (vHeight / 2).toFloat())
        mPath?.quadTo((vWidth / 2).toFloat(), (vHeight / 2).toFloat(), vWidth.toFloat(), (vHeight / 2).toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(mPath!!, mPaint!!)
    }
}