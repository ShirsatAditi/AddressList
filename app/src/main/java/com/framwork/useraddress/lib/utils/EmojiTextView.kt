package com.framwork.useraddress.lib.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.RemoteViews
import android.widget.TextView
import org.apache.commons.lang3.StringEscapeUtils

@RemoteViews.RemoteView
private class EmojiTextView : TextView {
    override fun setText(text: CharSequence?, type: BufferType?) {
        setText(StringEscapeUtils.unescapeJava(text.toString()), type)
    }
    constructor(context: Context) : super(context){
        setText(getText())
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        setText(getText())
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        setText(getText())
    }

}