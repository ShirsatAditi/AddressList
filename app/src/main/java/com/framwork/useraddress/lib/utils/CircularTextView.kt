package com.framwork.useraddress.lib.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * CircularTextView class is used to display custom circle background to a TextView.
 *
 * @author Aditi Shirsat
 */
class CircularTextView : AppCompatTextView {
    private var strokeWidth: Float = 0.toFloat()
    private var strokeColor: Int = 0
    private var bgColor: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun draw(canvas: Canvas) {

        val circlePaint = Paint()
        circlePaint.color = bgColor
        circlePaint.flags = Paint.ANTI_ALIAS_FLAG

        val strokePaint = Paint()
        strokePaint.color = strokeColor
        strokePaint.flags = Paint.ANTI_ALIAS_FLAG

        val h = this.height
        val w = this.width

        val diameter = if (h > w) h else w
        val radius = diameter / 2

        this.height = diameter
        this.width = diameter

        canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), strokePaint)

        canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius - strokeWidth, circlePaint)

        super.draw(canvas)
    }

    /**
     * Set stroke width.
     *
     * @param dp Density Pixel
     */
    fun setStrokeWidth(dp: Int) {
        val scale = context.resources.displayMetrics.density
        strokeWidth = dp * scale
    }

    /**
     * Set stroke color.
     *
     * @param color Color
     */
    fun setStrokeColor(color: String) {
        strokeColor = Color.parseColor(color)
    }

    /**
     * Set background color.
     *
     * @param color Color
     */
    fun setSolidColor(color: String) {
        bgColor = Color.parseColor(color)
    }
}