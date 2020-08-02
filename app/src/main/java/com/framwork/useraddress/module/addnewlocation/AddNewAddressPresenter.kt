package com.framwork.useraddress.module.addnewlocation

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.base.SuccessData
import com.framwork.useraddress.lib.retrofit.CustomRetroCallbacks
import com.framwork.useraddress.lib.utils.Constants

/**
 * AddNewAddressPresenter Presenter for [ActivityAddNewAddress]
 *
 * @author Aditi Shirsat
 */
class AddNewAddressPresenter(val mView: AddNewAddressContract.View) :
    AddNewAddressContract.Presenter {

    override fun start() {
    }

    override fun onDestroy() {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    override fun addAddress(
        mAddAddressData: MutableLiveData<Any>,
        address_line1: String,
        address_line2: String,
        city: String,
        pin_Code: String
    ) {
        mAddAddressData.value = true
        mView.getAPIComponent().retroService.getBaseUrl()
            .saveAddress(address_line1, address_line2, city, pin_Code, Constants.getUserId())
            .enqueue(object : CustomRetroCallbacks<SuccessData>() {
                override fun onSuccess(response: SuccessData?) {
                    mAddAddressData.value = false
                    mAddAddressData.value = response
                }

                override fun onError(response: SuccessData?) {
                    mAddAddressData.value = false
                    mAddAddressData.value = response?.message
                }

                override fun onFailure(generalErrorMsg: String, systemErrorMsg: String) {
                    mAddAddressData.value = false
                    mAddAddressData.value = generalErrorMsg
                }

            })
    }
}