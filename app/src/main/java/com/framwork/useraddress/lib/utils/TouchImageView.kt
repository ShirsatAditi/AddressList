package com.framwork.useraddress.lib.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * TouchImageView class is used to add pinch zoom, dragging, fling, double tap zoom functionality to an ImageView.
 * Note: TouchImageView does not yet support pinch image rotation. Also, FIT_START and FIT_END scaleTypes are not yet supported.
 *
 * @author Michael Ortiz
 */
class TouchImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AppCompatImageView(context, attrs, defStyle) {

    //
    // SuperMin and SuperMax multipliers. Determine how much the image can be
    // zoomed below or above the zoom boundaries, before animating back to the
    // min/max zoom boundary.
    //
    private val superMinMultiplier = .75f
    private val superMaxMultiplier = 1.25f
    private val zoomTime = 500f

    /**
     * If setMinZoom(automaticMinZoom), then we'll set the min scale to include the whole image.
     */
    private val automaticMinZoom = -1.0f

    /**
     * Get the current zoom. This is the zoom relative to the initial
     * scale, not the original resource.
     *
     * @return current zoom multiplier.
     */
    var currentZoom: Float = 0.toFloat()
        private set

    //
    // Matrix applied to image. M_SCALE_X and M_SCALE_Y should always be equal.
    // M_TRANS_X and M_TRANS_Y are the other values used. prevMatrix is the mMatrix
    // saved prior to the screen rotating.
    //
    private var mMatrix: Matrix? = null
    private var prevMatrix: Matrix? = null

    private var orientationChangeFixedPixel =
        FixedPixel.CENTER
    private var viewSizeChangeFixedPixel =
        FixedPixel.CENTER
    private var orientationJustChanged = false

    private var state: State? = null
    private var userSpecifiedMinScale: Float = 0.toFloat()
    private var minScale: Float = 0.toFloat()
    private var maxScaleIsSetByMultiplier = false
    private var maxScaleMultiplier: Float = 0.toFloat()
    private var maxScale: Float = 0.toFloat()
    private var superMinScale: Float = 0.toFloat()
    private var superMaxScale: Float = 0.toFloat()
    private var m: FloatArray? = null

    private var mContext: Context? = null
    private var fling: Fling? = null
    private var orientation: Int = 0

    private var mScaleType: ScaleType? = null

    private var imageRenderedAtLeastOnce: Boolean = false
    private var onDrawReady: Boolean = false

    private var delayedZoomVariables: ZoomVariables? = null

    //
    // Size of view and previous view size (ie before rotation)
    //
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var prevViewWidth: Int = 0
    private var prevViewHeight: Int = 0

    //
    // Size of image when it is stretched to fit view. Before and After rotation.
    //
    private var matchViewWidth: Float = 0.toFloat()
    private var matchViewHeight: Float = 0.toFloat()
    private var prevMatchViewWidth: Float = 0.toFloat()
    private var prevMatchViewHeight: Float = 0.toFloat()

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private var doubleTapListener: GestureDetector.OnDoubleTapListener? = null
    private var userTouchListener: OnTouchListener? = null
    private var touchImageViewListener: OnTouchImageViewListener? = null

    /**
     * Returns false if image is in initial, unzoomed state. False, otherwise.
     *
     * @return true if image is zoomed
     */
    private val isZoomed: Boolean
        get() = currentZoom != 1f

    // CENTER_CROP
    private var minZoom: Float
        get() = minScale
        set(min) {
            userSpecifiedMinScale = min
            if (min == automaticMinZoom) {
                if (mScaleType == ScaleType.CENTER || mScaleType == ScaleType.CENTER_CROP) {
                    val drawable = drawable
                    val drawableWidth = drawable!!.intrinsicWidth
                    val drawableHeight = drawable.intrinsicHeight
                    if (drawableWidth > 0 && drawableHeight > 0) {
                        val widthRatio = viewWidth.toFloat() / drawableWidth
                        val heightRatio = viewHeight.toFloat() / drawableHeight
                        minScale = if (mScaleType == ScaleType.CENTER) {
                            min(widthRatio, heightRatio)
                        } else {
                            min(widthRatio, heightRatio) / max(widthRatio, heightRatio)
                        }
                    }
                } else {
                    minScale = 1.0f
                }
            } else {
                minScale = userSpecifiedMinScale
            }
            if (maxScaleIsSetByMultiplier) {
                setMaxZoomRatio(maxScaleMultiplier)
            }
            superMinScale = superMinMultiplier * minScale
        }

    /**
     * Return the point at the center of the zoomed image. The PointF coordinates range
     * in value between 0 and 1 and the focus point is denoted as a fraction from the left
     * and top of the view. For example, the top left corner of the image would be (0, 0).
     * And the bottom right corner would be (1, 1).
     *
     * @return PointF representing the scroll position of the zoomed image.
     */
    private val scrollPosition: PointF?
        get() {
            val drawable = drawable ?: return null
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            val point = transformCoordTouchToBitmap((viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), true)
            point.x /= drawableWidth.toFloat()
            point.y /= drawableHeight.toFloat()
            return point
        }

    private val imageWidth: Float
        get() = matchViewWidth * currentZoom

    private val imageHeight: Float
        get() = matchViewHeight * currentZoom

    enum class FixedPixel {
        CENTER, TOP_LEFT, BOTTOM_RIGHT
    }

    private enum class State {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

    init {
        configureImageView(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configureImageView(context: Context) {
        this.mContext = context

        super.setClickable(true)

        orientation = resources.configuration.orientation
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mGestureDetector = GestureDetector(context, GestureListener())

        mMatrix = Matrix()
        prevMatrix = Matrix()

        m = FloatArray(9)
        currentZoom = 1f
        if (mScaleType == null) {
            mScaleType = ScaleType.FIT_CENTER
        }

        minScale = 1f
        maxScale = 3f

        superMinScale = superMinMultiplier * minScale
        superMaxScale = superMaxMultiplier * maxScale

        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX
        setState(State.NONE)

        onDrawReady = false

        super.setOnTouchListener(PrivateOnTouchListener())
    }

    override fun setOnTouchListener(l: OnTouchListener) {
        userTouchListener = l
    }

    override fun setImageResource(resId: Int) {
        imageRenderedAtLeastOnce = false
        super.setImageResource(resId)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageBitmap(bm: Bitmap) {
        imageRenderedAtLeastOnce = false
        super.setImageBitmap(bm)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        imageRenderedAtLeastOnce = false
        super.setImageDrawable(drawable)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageURI(uri: Uri?) {
        imageRenderedAtLeastOnce = false
        super.setImageURI(uri)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setScaleType(type: ScaleType?) {
        if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX)

        } else {
            mScaleType = type
            if (onDrawReady) {
                //
                // If the image is already rendered, scaleType has been called programmatically
                // and the TouchImageView should be updated with the new scaleType.
                //
                setZoom(this)
            }
        }
    }

    override fun getScaleType(): ScaleType? {
        return mScaleType
    }

    /**
     * Save the current mMatrix and view dimensions
     * in the prevMatrix and prevView variables.
     */
    private fun savePreviousImageValues() {
        if (mMatrix != null && viewHeight != 0 && viewWidth != 0) {
            mMatrix!!.getValues(m)
            prevMatrix!!.setValues(m)
            prevMatchViewHeight = matchViewHeight
            prevMatchViewWidth = matchViewWidth
            prevViewHeight = viewHeight
            prevViewWidth = viewWidth
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putInt("orientation", orientation)
        bundle.putFloat("saveScale", currentZoom)
        bundle.putFloat("matchViewHeight", matchViewHeight)
        bundle.putFloat("matchViewWidth", matchViewWidth)
        bundle.putInt("viewWidth", viewWidth)
        bundle.putInt("viewHeight", viewHeight)
        mMatrix!!.getValues(m)
        bundle.putFloatArray("mMatrix", m)
        bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce)
        bundle.putSerializable("viewSizeChangeFixedPixel", viewSizeChangeFixedPixel)
        bundle.putSerializable("orientationChangeFixedPixel", orientationChangeFixedPixel)
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            currentZoom = bundle.getFloat("saveScale")
            m = bundle.getFloatArray("mMatrix")
            prevMatrix!!.setValues(m)
            prevMatchViewHeight = bundle.getFloat("matchViewHeight")
            prevMatchViewWidth = bundle.getFloat("matchViewWidth")
            prevViewHeight = bundle.getInt("viewHeight")
            prevViewWidth = bundle.getInt("viewWidth")
            imageRenderedAtLeastOnce = bundle.getBoolean("imageRendered")
            viewSizeChangeFixedPixel = bundle.getSerializable("viewSizeChangeFixedPixel") as FixedPixel
            orientationChangeFixedPixel = bundle.getSerializable("orientationChangeFixedPixel") as FixedPixel
            val oldOrientation = bundle.getInt("orientation")
            if (orientation != oldOrientation) {
                orientationJustChanged = true
            }
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
            return
        }

        super.onRestoreInstanceState(state)
    }

    override fun onDraw(canvas: Canvas) {
        onDrawReady = true
        imageRenderedAtLeastOnce = true
        if (delayedZoomVariables != null) {
            setZoom(
                delayedZoomVariables!!.scale,
                delayedZoomVariables!!.focusX,
                delayedZoomVariables!!.focusY,
                delayedZoomVariables!!.scaleType
            )
            delayedZoomVariables = null
        }
        super.onDraw(canvas)
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newOrientation = resources.configuration.orientation
        if (newOrientation != orientation) {
            orientationJustChanged = true
            orientation = newOrientation
        }
        savePreviousImageValues()
    }

    /**
     * Set the max zoom multiplier as a multiple of minZoom, whatever minZoom may change to. By
     * default, this is not done, and maxZoom has a fixed value of 3.
     *
     * @param max max zoom multiplier, as a multiple of minZoom
     */
    private fun setMaxZoomRatio(max: Float) {
        maxScaleMultiplier = max
        maxScale = minScale * maxScaleMultiplier
        superMaxScale = superMaxMultiplier * maxScale
        maxScaleIsSetByMultiplier = true
    }

    /**
     * Reset zoom and translation to initial state.
     */
    private fun resetZoom() {
        currentZoom = 1f
        fitImageToView()
    }

    /**
     * Set zoom to the specified scale. Image will be centered around the point
     * (focusX, focusY). These floats range from 0 to 1 and denote the focus point
     * as a fraction from the left and top of the view. For example, the top left
     * corner of the image would be (0, 0). And the bottom right corner would be (1, 1).
     *
     * @param scale
     * @param focusX
     * @param focusY
     * @param scaleType
     */
    @JvmOverloads
    fun setZoom(
        scale: Float,
        focusX: Float = 0.5f,
        focusY: Float = 0.5f,
        scaleType: ScaleType? = mScaleType
    ) {
        //
        // setZoom can be called before the image is on the screen, but at this point,
        // image and view sizes have not yet been calculated in onMeasure. Thus, we should
        // delay calling setZoom until the view has been measured.
        //
        if (!onDrawReady) {
            delayedZoomVariables = ZoomVariables(scale, focusX, focusY, scaleType)
            return
        }
        if (userSpecifiedMinScale == automaticMinZoom) {
            minZoom = automaticMinZoom
            if (currentZoom < minScale) {
                currentZoom = minScale
            }
        }

        if (scaleType != mScaleType) {
            setScaleType(scaleType)
        }
        resetZoom()
        scaleImage(scale.toDouble(), (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), true)
        mMatrix!!.getValues(m)
        m?.set(Matrix.MTRANS_X, -(focusX * imageWidth - viewWidth * 0.5f))
        m?.set(Matrix.MTRANS_Y, -(focusY * imageHeight - viewHeight * 0.5f))
        mMatrix!!.setValues(m)
        fixTrans()
        imageMatrix = mMatrix
    }

    /**
     * Set zoom parameters equal to another TouchImageView. Including scale, position,
     * and ScaleType.
     *
     * @param img
     */
    private fun setZoom(img: TouchImageView) {
        val center = img.scrollPosition
        setZoom(img.currentZoom, center!!.x, center.y, img.scaleType)
    }

    /**
     * Performs boundary checking and fixes the image mMatrix if it
     * is out of bounds.
     */
    private fun fixTrans() {
        mMatrix!!.getValues(m)
        val transX = m!![Matrix.MTRANS_X]
        val transY = m!![Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), imageWidth)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), imageHeight)

        if (fixTransX != 0f || fixTransY != 0f) {
            mMatrix!!.postTranslate(fixTransX, fixTransY)
        }
    }

    /**
     * When transitioning from zooming from focus to zoom from center (or vice versa)
     * the image can become unaligned within the view. This is apparent when zooming
     * quickly. When the content size is less than the view size, the content will often
     * be centered incorrectly within the view. fixScaleTrans first calls fixTrans() and
     * then makes sure the image is centered correctly within the view.
     */
    private fun fixScaleTrans() {
        fixTrans()
        mMatrix!!.getValues(m)
        if (imageWidth < viewWidth) {
            m?.set(Matrix.MTRANS_X, (viewWidth - imageWidth) / 2)
        }

        if (imageHeight < viewHeight) {
            m?.set(Matrix.MTRANS_Y, (viewHeight - imageHeight) / 2)
        }
        mMatrix!!.setValues(m)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize

        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans)
            return -trans + minTrans
        return if (trans > maxTrans) -trans + maxTrans else 0f
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0f
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            setMeasuredDimension(0, 0)
            return
        }

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val totalViewWidth = setViewSize(widthMode, widthSize, drawableWidth)
        val totalViewHeight = setViewSize(heightMode, heightSize, drawableHeight)

        if (!orientationJustChanged) {
            savePreviousImageValues()
        }

        // Image view width, height must consider padding
        val width = totalViewWidth - paddingLeft - paddingRight
        val height = totalViewHeight - paddingTop - paddingBottom

        //
        // Set view dimensions
        //
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //
        // Fit content within view.
        //
        // onMeasure may be called multiple times for each layout change, including orientation
        // changes. For example, if the TouchImageView is inside a ConstraintLayout, onMeasure may
        // be called with:
        // widthMeasureSpec == "AT_MOST 2556" and then immediately with
        // widthMeasureSpec == "EXACTLY 1404", then back and forth multiple times in quick
        // succession, as the ConstraintLayout tries to solve its constraints.
        //
        // onSizeChanged is called once after the final onMeasure is called. So we make all changes
        // to class members, such as fitting the image into the new shape of the TouchImageView,
        // here, after the final size has been determined. This helps us avoid both
        // repeated computations, and making irreversible changes (e.g. making the View temporarily too
        // big or too small, thus making the current zoom fall outside of an automatically-changing
        // minZoom and maxZoom).
        //
        viewWidth = w
        viewHeight = h
        fitImageToView()
    }

    /**
     * This function can be called:
     * 1. When the TouchImageView is first loaded (onMeasure).
     * 2. When a new image is loaded (setImageResource|Bitmap|Drawable|URI).
     * 3. On rotation (onSaveInstanceState, then onRestoreInstanceState, then onMeasure).
     * 4. When the view is resized (onMeasure).
     * 5. When the zoom is reset (resetZoom).
     *
     *
     * In cases 2, 3 and 4, we try to maintain the zoom state and position as directed by
     * orientationChangeFixedPixel or viewSizeChangeFixedPixel (if there is an existing zoom state
     * and position, which there might not be in case 2).
     *
     *
     * If the normalizedScale is equal to 1, then the image is made to fit the View. Otherwise, we
     * maintain zoom level and attempt to roughly put the same part of the image in the View as was
     * there before, paying attention to orientationChangeFixedPixel or viewSizeChangeFixedPixel.
     */
    private fun fitImageToView() {
        val fixedPixel = if (orientationJustChanged)
            orientationChangeFixedPixel
        else
            viewSizeChangeFixedPixel
        orientationJustChanged = false

        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return
        }
        if (mMatrix == null || prevMatrix == null) {
            return
        }

        if (userSpecifiedMinScale == automaticMinZoom) {
            minZoom = automaticMinZoom
            if (currentZoom < minScale) {
                currentZoom = minScale
            }
        }

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        //
        // Scale image for view
        //
        var scaleX = viewWidth.toFloat() / drawableWidth
        var scaleY = viewHeight.toFloat() / drawableHeight

        when (mScaleType) {
            ScaleType.CENTER -> {
                scaleY = 1f
                scaleX = scaleY
            }
            ScaleType.CENTER_CROP -> {
                scaleY = max(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.CENTER_INSIDE -> {
                scaleY = min(1f, min(scaleX, scaleY))
                scaleX = scaleY
                scaleY = min(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END -> {
                scaleY =
                    min(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_XY -> {
            }
            else -> {

            }
        }

        //
        // Put the image's center in the right place.
        //
        val redundantXSpace = viewWidth - scaleX * drawableWidth
        val redundantYSpace = viewHeight - scaleY * drawableHeight
        matchViewWidth = viewWidth - redundantXSpace
        matchViewHeight = viewHeight - redundantYSpace
        if (!isZoomed && !imageRenderedAtLeastOnce) {
            //
            // Stretch and center image to fit view
            //
            mMatrix!!.setScale(scaleX, scaleY)
            when (mScaleType) {
                ScaleType.FIT_START -> mMatrix!!.postTranslate(0f, 0f)
                ScaleType.FIT_END -> mMatrix!!.postTranslate(redundantXSpace, redundantYSpace)
                else -> mMatrix!!.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            }
            currentZoom = 1f
        } else {
            //
            // These values should never be 0 or we will set viewWidth and viewHeight
            // to NaN in newTranslationAfterChange. To avoid this, call savePreviousImageValues
            // to set them equal to the current values.
            //
            if (prevMatchViewWidth == 0f || prevMatchViewHeight == 0f) {
                savePreviousImageValues()
            }

            //
            // Use the previous mMatrix as our starting point for the new mMatrix.
            //
            prevMatrix!!.getValues(m)

            //
            // Rescale Matrix if appropriate
            //
            m?.set(Matrix.MSCALE_X, matchViewWidth / drawableWidth * currentZoom)
            m?.set(Matrix.MSCALE_Y, matchViewHeight / drawableHeight * currentZoom)

            //
            // TransX and TransY from previous mMatrix
            //
            val transX = m!![Matrix.MTRANS_X]
            val transY = m!![Matrix.MTRANS_Y]

            //
            // X position
            //
            val prevActualWidth = prevMatchViewWidth * currentZoom
            val actualWidth = imageWidth
            m!![Matrix.MTRANS_X] = newTranslationAfterChange(
                transX,
                prevActualWidth,
                actualWidth,
                prevViewWidth,
                viewWidth,
                drawableWidth,
                fixedPixel
            )

            //
            // Y position
            //
            val prevActualHeight = prevMatchViewHeight * currentZoom
            val actualHeight = imageHeight
            m!![Matrix.MTRANS_Y] = newTranslationAfterChange(
                transY,
                prevActualHeight,
                actualHeight,
                prevViewHeight,
                viewHeight,
                drawableHeight,
                fixedPixel
            )

            //
            // Set the mMatrix to the adjusted scale and translation values.
            //
            mMatrix!!.setValues(m)
        }
        fixTrans()
        imageMatrix = mMatrix
    }

    /**
     * Set view dimensions based on layout params
     *
     * @param mode
     * @param size
     * @param drawableWidth
     * @return
     */
    private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
        return when (mode) {
            MeasureSpec.EXACTLY -> size

            MeasureSpec.AT_MOST -> min(drawableWidth, size)

            MeasureSpec.UNSPECIFIED -> drawableWidth

            else -> size
        }
    }

    /**
     * After any change described in the comments for fitImageToView, the mMatrix needs to be
     * translated. This function translates the image so that the fixed pixel in the image
     * stays in the same place in the View.
     *
     * @param trans                the value of trans in that axis before the rotation
     * @param prevImageSize        the width/height of the image before the rotation
     * @param imageSize            width/height of the image after rotation
     * @param prevViewSize         width/height of view before rotation
     * @param viewSize             width/height of view after rotation
     * @param drawableSize         width/height of drawable
     * @param sizeChangeFixedPixel how we should choose the fixed pixel
     */
    private fun newTranslationAfterChange(
        trans: Float,
        prevImageSize: Float,
        imageSize: Float,
        prevViewSize: Int,
        viewSize: Int,
        drawableSize: Int,
        sizeChangeFixedPixel: FixedPixel
    ): Float {
        when {
            imageSize < viewSize ->
                //
                // The width/height of image is less than the view's width/height. Center it.
                //
                return (viewSize - drawableSize * m!![Matrix.MSCALE_X]) * 0.5f
            trans > 0 ->
                //
                // The image is larger than the view, but was not before the view changed. Center it.
                //
                return -((imageSize - viewSize) * 0.5f)
            else -> {
                //
                // Where is the pixel in the View that we are keeping stable, as a fraction of the
                // width/height of the View?
                //
                var fixedPixelPositionInView = 0.5f  // CENTER
                if (sizeChangeFixedPixel == FixedPixel.BOTTOM_RIGHT) {
                    fixedPixelPositionInView = 1.0f
                } else if (sizeChangeFixedPixel == FixedPixel.TOP_LEFT) {
                    fixedPixelPositionInView = 0.0f
                }
                //
                // Where is the pixel in the Image that we are keeping stable, as a fraction of the
                // width/height of the Image?
                //
                val fixedPixelPositionInImage = (-trans + fixedPixelPositionInView * prevViewSize) / prevImageSize
                //
                // Here's what the new translation should be so that, after whatever change triggered
                // this function to be called, the pixel at fixedPixelPositionInView of the View is
                // still the pixel at fixedPixelPositionInImage of the image.
                //
                return -(fixedPixelPositionInImage * imageSize - viewSize * fixedPixelPositionInView)
            }
        }
    }

    private fun setState(state: State) {
        this.state = state
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        mMatrix!!.getValues(m)
        val x = m!![Matrix.MTRANS_X]

        return if (imageWidth < viewWidth) {
            false

        } else if (x >= -1 && direction < 0) {
            false

        } else
            abs(x) + viewWidth.toFloat() + 1f < imageWidth || direction <= 0

    }

    override fun canScrollVertically(direction: Int): Boolean {
        mMatrix!!.getValues(m)
        val y = m!![Matrix.MTRANS_Y]

        return if (imageHeight < viewHeight) {
            false

        } else if (y >= -1 && direction < 0) {
            false

        } else
            abs(y) + viewHeight.toFloat() + 1f < imageHeight || direction <= 0

    }

    /**
     * Gesture Listener detects a single click or long click and passes that on
     * to the view's listener.
     *
     * @author Ortiz
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return if (doubleTapListener != null) {
                doubleTapListener!!.onSingleTapConfirmed(e)
            } else performClick()
        }

        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (fling != null) {
                //
                // If a previous fling is still active, it should be cancelled so that two flings
                // are not run simultaneously.
                //
                fling!!.cancelFling()
            }
            fling = Fling(velocityX.toInt(), velocityY.toInt())
            compatPostOnAnimation(fling!!)
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            var consumed = false
            if (doubleTapListener != null) {
                consumed = doubleTapListener!!.onDoubleTap(e)
            }
            if (state == State.NONE) {
                val targetZoom = if (currentZoom == minScale) maxScale else minScale
                val doubleTap = DoubleTapZoom(targetZoom, e.x, e.y, false)
                compatPostOnAnimation(doubleTap)
                consumed = true
            }
            return consumed
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return if (doubleTapListener != null) {
                doubleTapListener!!.onDoubleTapEvent(e)
            } else false
        }
    }

    interface OnTouchImageViewListener {
        fun onMove()
    }

    /**
     * Responsible for all touch events. Handles the heavy lifting of drag and also sends
     * touch events to Scale Detector and Gesture Detector.
     *
     * @author Ortiz
     */
    private inner class PrivateOnTouchListener : OnTouchListener {

        //
        // Remember last point position for dragging
        //
        private val last = PointF()

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (drawable == null) {
                setState(State.NONE)
                return false
            }
            mScaleDetector!!.onTouchEvent(event)
            mGestureDetector!!.onTouchEvent(event)
            val curr = PointF(event.x, event.y)

            if (state == State.NONE || state == State.DRAG || state == State.FLING) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        last.set(curr)
                        if (fling != null)
                            fling!!.cancelFling()
                        setState(State.DRAG)
                    }

                    MotionEvent.ACTION_MOVE -> if (state == State.DRAG) {
                        val deltaX = curr.x - last.x
                        val deltaY = curr.y - last.y
                        val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(), imageWidth)
                        val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(), imageHeight)
                        mMatrix!!.postTranslate(fixTransX, fixTransY)
                        fixTrans()
                        last.set(curr.x, curr.y)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> setState(State.NONE)
                }
            }

            imageMatrix = mMatrix

            //
            // User-defined OnTouchListener
            //
            if (userTouchListener != null) {
                userTouchListener!!.onTouch(v, event)
            }

            //
            // OnTouchImageViewListener is set: TouchImageView dragged by user.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener!!.onMove()
            }

            //
            // indicate event was handled
            //
            return true
        }
    }

    /**
     * ScaleListener detects user two finger scaling and scales image.
     *
     * @author Ortiz
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            setState(State.ZOOM)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleImage(detector.scaleFactor.toDouble(), detector.focusX, detector.focusY, true)

            //
            // OnTouchImageViewListener is set: TouchImageView pinch zoomed by user.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener!!.onMove()
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            setState(State.NONE)
            var animateToZoomBoundary = false
            var targetZoom = currentZoom
            if (currentZoom > maxScale) {
                targetZoom = maxScale
                animateToZoomBoundary = true

            } else if (currentZoom < minScale) {
                targetZoom = minScale
                animateToZoomBoundary = true
            }

            if (animateToZoomBoundary) {
                val doubleTap = DoubleTapZoom(targetZoom, (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), true)
                compatPostOnAnimation(doubleTap)
            }
        }
    }

    private fun scaleImage(deltaScale: Double, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) {
        var mDeltaScale = deltaScale
        val lowerScale: Float
        val upperScale: Float
        if (stretchImageToSuper) {
            lowerScale = superMinScale
            upperScale = superMaxScale

        } else {
            lowerScale = minScale
            upperScale = maxScale
        }

        val origScale = currentZoom
        currentZoom *= mDeltaScale.toFloat()
        if (currentZoom > upperScale) {
            currentZoom = upperScale
            mDeltaScale = (upperScale / origScale).toDouble()
        } else if (currentZoom < lowerScale) {
            currentZoom = lowerScale
            mDeltaScale = (lowerScale / origScale).toDouble()
        }

        mMatrix!!.postScale(mDeltaScale.toFloat(), mDeltaScale.toFloat(), focusX, focusY)
        fixScaleTrans()
    }

    /**
     * DoubleTapZoom calls a series of runnables which apply
     * an animated zoom in/out graphic to the image.
     *
     * @author Ortiz
     */
    private inner class DoubleTapZoom internal constructor(
        private val targetZoom: Float,
        focusX: Float,
        focusY: Float,
        private val stretchImageToSuper: Boolean
    ) : Runnable {

        private val startTime: Long
        private val startZoom: Float
        private val bitmapX: Float
        private val bitmapY: Float
        private val interpolator = AccelerateDecelerateInterpolator()
        private val startTouch: PointF
        private val endTouch: PointF

        init {
            setState(State.ANIMATE_ZOOM)
            startTime = System.currentTimeMillis()
            this.startZoom = currentZoom
            val bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false)
            this.bitmapX = bitmapPoint.x
            this.bitmapY = bitmapPoint.y

            //
            // Used for translating image during scaling
            //
            startTouch = transformBitmapToTouch(bitmapX, bitmapY)
            endTouch = PointF((viewWidth / 2).toFloat(), (viewHeight / 2).toFloat())
        }

        override fun run() {
            if (drawable == null) {
                setState(State.NONE)
                return
            }
            val t = interpolate()
            val deltaScale = calculateDeltaScale(t)
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper)
            translateImageToCenterTouchPosition(t)
            fixScaleTrans()
            imageMatrix = mMatrix

            //
            // OnTouchImageViewListener is set: double tap runnable updates listener
            // with every frame.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener!!.onMove()
            }

            if (t < 1f) {
                //
                // We haven't finished zooming
                //
                compatPostOnAnimation(this)

            } else {
                //
                // Finished zooming
                //
                setState(State.NONE)
            }
        }

        /**
         * Interpolate between where the image should start and end in order to translate
         * the image so that the point that is touched is what ends up centered at the end
         * of the zoom.
         *
         * @param t
         */
        private fun translateImageToCenterTouchPosition(t: Float) {
            val targetX = startTouch.x + t * (endTouch.x - startTouch.x)
            val targetY = startTouch.y + t * (endTouch.y - startTouch.y)
            val curr = transformBitmapToTouch(bitmapX, bitmapY)
            mMatrix!!.postTranslate(targetX - curr.x, targetY - curr.y)
        }

        /**
         * Use interpolator to get t
         *
         * @return
         */
        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed = (currTime - startTime) / zoomTime
            elapsed = min(1f, elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        /**
         * Interpolate the current targeted zoom and get the delta
         * from the current zoom.
         *
         * @param t
         * @return
         */
        private fun calculateDeltaScale(t: Float): Double {
            val zoom = (startZoom + t * (targetZoom - startZoom)).toDouble()
            return zoom / currentZoom
        }
    }

    /**
     * This function will transform the coordinates in the touch event to the coordinate
     * system of the drawable that the imageview contain
     *
     * @param x            x-coordinate of touch event
     * @param y            y-coordinate of touch event
     * @param clipToBitmap Touch event may occur within view, but outside image content. True, to clip return value
     * to the bounds of the bitmap size.
     * @return Coordinates of the point touched, in the coordinate system of the original drawable.
     */
    private fun transformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        mMatrix!!.getValues(m)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val transX = m!![Matrix.MTRANS_X]
        val transY = m!![Matrix.MTRANS_Y]
        var finalX = (x - transX) * origW / imageWidth
        var finalY = (y - transY) * origH / imageHeight

        if (clipToBitmap) {
            finalX = min(max(finalX, 0f), origW)
            finalY = min(max(finalY, 0f), origH)
        }

        return PointF(finalX, finalY)
    }

    /**
     * Inverse of transformCoordTouchToBitmap. This function will transform the coordinates in the
     * drawable's coordinate system to the view's coordinate system.
     *
     * @param bx x-coordinate in original bitmap coordinate system
     * @param by y-coordinate in original bitmap coordinate system
     * @return Coordinates of the point in the view's coordinate system.
     */
    private fun transformBitmapToTouch(bx: Float, by: Float): PointF {
        mMatrix!!.getValues(m)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val px = bx / origW
        val py = by / origH
        val finalX = m!![Matrix.MTRANS_X] + imageWidth * px
        val finalY = m!![Matrix.MTRANS_Y] + imageHeight * py
        return PointF(finalX, finalY)
    }

    /**
     * Fling launches sequential runnables which apply
     * the fling graphic to the image. The values for the translation
     * are interpolated by the Scroller.
     *
     * @author Ortiz
     */
    private inner class Fling internal constructor(velocityX: Int, velocityY: Int) : Runnable {

        internal var scroller: CompatScroller? = null
        internal var currX: Int = 0
        internal var currY: Int = 0

        init {
            setState(State.FLING)
            scroller = CompatScroller(mContext)
            mMatrix!!.getValues(m)

            val startX = m!![Matrix.MTRANS_X].toInt()
            val startY = m!![Matrix.MTRANS_Y].toInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int

            if (imageWidth > viewWidth) {
                minX = viewWidth - imageWidth.toInt()
                maxX = 0

            } else {
                maxX = startX
                minX = maxX
            }

            if (imageHeight > viewHeight) {
                minY = viewHeight - imageHeight.toInt()
                maxY = 0

            } else {
                maxY = startY
                minY = maxY
            }

            scroller!!.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
            currX = startX
            currY = startY
        }

        fun cancelFling() {
            if (scroller != null) {
                setState(State.NONE)
                scroller!!.forceFinished(true)
            }
        }

        override fun run() {

            //
            // OnTouchImageViewListener is set: TouchImageView listener has been flung by user.
            // Listener runnable updated with each frame of fling animation.
            //
            if (touchImageViewListener != null) {
                touchImageViewListener!!.onMove()
            }

            if (scroller!!.isFinished) {
                scroller = null
                return
            }

            if (scroller!!.computeScrollOffset()) {
                val newX = scroller!!.currX
                val newY = scroller!!.currY
                val transX = newX - currX
                val transY = newY - currY
                currX = newX
                currY = newY
                mMatrix!!.postTranslate(transX.toFloat(), transY.toFloat())
                fixTrans()
                imageMatrix = mMatrix
                compatPostOnAnimation(this)
            }
        }
    }

    @TargetApi(VERSION_CODES.GINGERBREAD)
    private inner class CompatScroller internal constructor(context: Context?) {
        internal var overScroller: OverScroller = OverScroller(context)

        val isFinished: Boolean
            get() = overScroller.isFinished

        internal val currX: Int
            get() = overScroller.currX

        internal val currY: Int
            get() = overScroller.currY

        internal fun fling(
            startX: Int,
            startY: Int,
            velocityX: Int,
            velocityY: Int,
            minX: Int,
            maxX: Int,
            minY: Int,
            maxY: Int
        ) {
            overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        }

        internal fun forceFinished(finished: Boolean) {
            overScroller.forceFinished(finished)
        }

        internal fun computeScrollOffset(): Boolean {
            overScroller.computeScrollOffset()
            return overScroller.computeScrollOffset()
        }
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private fun compatPostOnAnimation(runnable: Runnable) {
        postOnAnimation(runnable)
    }

    private inner class ZoomVariables internal constructor(
        internal var scale: Float,
        internal var focusX: Float,
        internal var focusY: Float,
        internal var scaleType: ScaleType?
    )
}
