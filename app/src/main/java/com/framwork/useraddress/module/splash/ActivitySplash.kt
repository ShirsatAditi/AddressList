package com.framwork.useraddress.module.splash

import android.os.Handler
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.BaseActivity
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.utils.startActivity
import com.framwork.useraddress.module.onboarding.signin.ActivitySignIn


/**
 * SplashActivity
 *
 * @author Aditi Shirsat
 */
class ActivitySplash : BaseActivity() {

    override fun setPresenter() {
    }

    override fun getPresenter(): BasePresenter? {
        return null
    }

    override fun getLayout(): Int {
        return R.layout.activity_splash
    }

    override fun setListener() {
    }

    override fun setObservers() {
    }

    override fun init() {
        Handler().postDelayed({startActivity<ActivitySignIn>()},500)
    }
}
