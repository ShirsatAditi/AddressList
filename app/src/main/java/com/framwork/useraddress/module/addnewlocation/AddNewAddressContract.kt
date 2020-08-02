package com.framwork.useraddress.module.addnewlocation

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.base.BaseView

/**
 * AddNewAddressContract interface for [ActivityAddNewAddress] and [AddNewAddressPresenter]
 *
 * @author Aditi Shirsat
 */
interface AddNewAddressContract {
    interface View : BaseView
    interface Presenter : BasePresenter {
        fun addAddress(mAddAddressData:MutableLiveData<Any>,address_line1:String,address_line2:String,city:String,pin_Code:String)
    }
}