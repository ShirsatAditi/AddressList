package com.framwork.useraddress.module.chooselocation

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.base.AddressData
import com.framwork.useraddress.lib.retrofit.CustomRetroCallbacks
import com.framwork.useraddress.lib.utils.Constants
import com.framwork.useraddress.model.UserData

/**
 * ChooseLocationPresenter Presenter for [ActivityChooseLocation]
 *
 * @author Aditi Shirsat
 */
class ChooseLocationPresenter (val mView:ChooseLocationContract.View):ChooseLocationContract.Presenter{

    override fun start() {
    }

    override fun onDestroy() {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    override fun getAddress(mAddressData: MutableLiveData<Any>) {
        mAddressData.value = true
        mView.getAPIComponent().retroService.getBaseUrl().getAddress(Constants.getUserId())
            .enqueue(object : CustomRetroCallbacks<AddressData>(){
                override fun onSuccess(response: AddressData?) {
                    mAddressData.value =false
                    mAddressData.value = response
                }

                override fun onError(response: AddressData?) {
                    mAddressData.value =false
                    mAddressData.value = response?.message
                }

                override fun onFailure(generalErrorMsg: String, systemErrorMsg: String) {
                    mAddressData.value =false
                    mAddressData.value = generalErrorMsg
                }
            })
    }
}