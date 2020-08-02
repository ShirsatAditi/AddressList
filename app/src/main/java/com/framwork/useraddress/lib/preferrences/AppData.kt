package com.framwork.useraddress.lib.preferrences

import android.content.Context
import android.content.SharedPreferences

/**
 * @author Aditi Shirsat
 */
class AppData (context: Context) {

    private val DEFAULT = "default"
    private val mContext: Context = context
    private val mPreferences: SharedPreferences
    private val mPreferenceEditor: SharedPreferences.Editor
    private val TITLE = "title"
    init {
        mPreferences = mContext.getSharedPreferences(DEFAULT,Context.MODE_PRIVATE)
        mPreferenceEditor = mPreferences.edit()
        mPreferenceEditor.apply()
    }

    fun storeTitle(title: String){
        mPreferenceEditor.putString(TITLE,title)
        mPreferenceEditor.apply()
    }

    fun getTitle(): String?{
        return mPreferences.getString(TITLE,null)
    }



}