package com.framwork.useraddress.lib.base

/**
 * @author Aditi Shirsat
 */
interface BasePresenter {
    fun start()
    fun onDestroy()
    fun onPause()
    fun onStop()
}