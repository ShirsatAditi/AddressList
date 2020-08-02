package com.framwork.useraddress.module.onboarding.signup

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.retrofit.CustomRetroCallbacks
import com.framwork.useraddress.lib.utils.Utility
import com.framwork.useraddress.model.UserData

/**
 * SignUpPresenter
 *
 * @author Aditi Shirsat
 */
class SignUpPresenter(val mView: SignUpContract.View) : SignUpContract.Presenter {

    override fun start() {
    }

    override fun onDestroy() {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    /**
     * validation for input parameter
     *
     * @param username:String not blank
     * @param email: String can not be blank and valid email ID
     * @param password: String can not be blank, and length should be in between 8 to 16
     *
     * @return isValid:Boolean if all strings are valid return true
     * */

    private fun validation(
        username: String,
        email: String,
        password: String,
        mSignUpData: MutableLiveData<Any>
    ): Boolean {
        when {
            Utility.isBlankString(username) -> mSignUpData.postValue("User name can not be blank")

            (Utility.isBlankString(email) || Utility.isEmailInvalid(email)) -> mSignUpData.postValue(
                "Please enter valid email address"
            )

            (Utility.isBlankString(password) || password.length > 16 || password.length < 8) -> mSignUpData.postValue(
                "Password length should be in between 8 to 16 characters"
            )

            else -> return true
        }
        return false
    }

    override fun signUp(
        mSignUpData: MutableLiveData<Any>,
        username: String,
        email: String,
        password: String
    ) {
        if (validation(username, email, password, mSignUpData)) {
            mSignUpData.value = true
            mView.getAPIComponent().retroService.getBaseUrl().register(username, email, password)
                .enqueue(object : CustomRetroCallbacks<UserData>() {
                    override fun onSuccess(response: UserData?) {
                        mSignUpData.value = false
                        mSignUpData.value = response
                    }

                    override fun onError(response: UserData?) {
                        mSignUpData.value = false
                        mSignUpData.value = response?.message
                    }

                    override fun onFailure(generalErrorMsg: String, systemErrorMsg: String) {
                        mSignUpData.value = false
                        mSignUpData.value = generalErrorMsg
                    }
                })
        }
    }
}