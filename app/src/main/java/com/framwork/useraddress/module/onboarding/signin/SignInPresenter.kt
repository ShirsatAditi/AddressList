package com.framwork.useraddress.module.onboarding.signin

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.retrofit.CustomRetroCallbacks
import com.framwork.useraddress.model.UserData

/**
 * SignInPresenter pesenter for [ActivitySignIn]
 *
 * @author Aditi Shirsat
 */
class SignInPresenter(val mView: SignInContract.View) : SignInContract.Presenter {

    override fun start() {
    }

    override fun onDestroy() {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    override fun signIn(mSignInData: MutableLiveData<Any>, username: String, password: String) {
        mSignInData.value = true
        mView.getAPIComponent().retroService.getBaseUrl().login(username, password)
            .enqueue(object : CustomRetroCallbacks<UserData>() {
                override fun onSuccess(response: UserData?) {
                    mSignInData.value = response
                    mSignInData.value = false
                }

                override fun onError(response: UserData?) {
                    mSignInData.value = response?.message
                    mSignInData.value = false
                }

                override fun onFailure(generalErrorMsg: String, systemErrorMsg: String) {
                    mSignInData.value = generalErrorMsg
                    mSignInData.value = false
                }
            })
    }
}