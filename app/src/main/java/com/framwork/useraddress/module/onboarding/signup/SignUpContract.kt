package com.framwork.useraddress.module.onboarding.signup

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.base.BaseView

/**
 * SignUpContract interface for [ActivitySignUp] and [SignUpPresenter]
 *
 * @author Aditi Shirsat
 */
interface SignUpContract {
    interface View : BaseView
    interface Presenter : BasePresenter{
        fun signUp(mSignUpData:MutableLiveData<Any>,username:String,email:String,password:String)
    }
}