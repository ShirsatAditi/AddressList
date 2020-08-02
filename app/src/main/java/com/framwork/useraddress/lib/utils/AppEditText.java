package com.framwork.useraddress.lib.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RemoteViews;

@SuppressLint("AppCompatCustomView")
@RemoteViews.RemoteView
public class AppEditText extends EditText {
    public AppEditText(Context context) {
        super(context);
    }

    public AppEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AppEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public Editable getText() {
        Editable s = (Editable) super.getText();
        String temp = s.toString();
        s.toString().replace("",temp);
        return s;
    }
}
