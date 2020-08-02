package com.framwork.useraddress.module.chooselocation

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.base.BaseView

/**
 * ChooseLocationContract interface for [ActivityChooseLocation] and [ChooseLocationPresenter]
 *
 * @author Aditi Shirsat
 */
interface ChooseLocationContract {
    interface View : BaseView
    interface Presenter : BasePresenter{
        fun getAddress(mAddressData:MutableLiveData<Any>)
    }
}