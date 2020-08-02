package com.framwork.useraddress.module.onboarding.signin

import androidx.lifecycle.MutableLiveData
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.base.BaseView

/**
 * SignInContract interface between [ActivitySignIn] [SignInPresenter]
 *
 * @author Aditi Shirsat
 */
interface SignInContract {
    interface View : BaseView
    interface Presenter : BasePresenter {
        fun signIn(mSignInData: MutableLiveData<Any>, username: String, password: String)
    }
}