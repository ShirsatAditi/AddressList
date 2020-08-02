package com.framwork.useraddress.lib.utils


import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

/**
 * CustomSpinner class is used to handle spinner open and close triggers.
 *
 * @author Aditi Shirsat
 */
class CustomSpinner : AppCompatSpinner {
    private var listener: OnSpinnerEventsListener? = null
    private var openInitiated = false

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, mode: Int) : super(
        context,
        attrs,
        defStyleAttr,
        mode
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, mode: Int) : super(context, mode)
    constructor(context: Context) : super(context)

    override fun performClick(): Boolean {
        /*
         register that the Spinner was opened so we have a status
         indicator for the activity(which may lose focus for some other reasons)
         */
        openInitiated = true
        listener?.onSpinnerOpened()
        return super.performClick()
    }

    fun setSpinnerEventsListener(onSpinnerEventsListener: OnSpinnerEventsListener) {
        listener = onSpinnerEventsListener
    }

    /**
     * Propagate the closed Spinner event to the listener from outside.
     */
    private fun performClosedEvent() {
        openInitiated = false
        listener?.onSpinnerClosed()
    }

    /**
     * A boolean flag indicating that the Spinner triggered an open event.
     *
     * @return true for opened Spinner
     */
    private fun hasBeenOpened(): Boolean {
        return openInitiated
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasBeenOpened() && hasWindowFocus) {
            performClosedEvent()
        }
    }

    interface OnSpinnerEventsListener {
        fun onSpinnerOpened()
        fun onSpinnerClosed()
    }
}