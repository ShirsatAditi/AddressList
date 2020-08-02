package com.framwork.useraddress.lib.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * @author Aditi Shirsat
 * This is Custom textview to show emoji.
 * if Imoji not present it will reflect the normal text.
 */

@RemoteView
public class AppTextView extends TextView {
    public AppTextView(Context context) {
        super(context);
        setText(getText());
    }

    public AppTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setText(getText());
    }

    public AppTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setText(getText());
    }

    public AppTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setText(getText());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text != null) {
            super.setText(StringEscapeUtils.unescapeJava(text.toString()), type);
        }
    }
}
