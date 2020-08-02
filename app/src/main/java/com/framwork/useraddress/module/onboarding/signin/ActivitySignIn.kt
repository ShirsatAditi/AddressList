package com.framwork.useraddress.module.onboarding.signin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.BaseActivity
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.utils.Utility
import com.framwork.useraddress.lib.utils.hide
import com.framwork.useraddress.lib.utils.show
import com.framwork.useraddress.lib.utils.startActivity
import com.framwork.useraddress.model.UserData
import com.framwork.useraddress.module.onboarding.signup.ActivitySignUp
import com.framwork.useraddress.module.chooselocation.ActivityChooseLocation
import kotlinx.android.synthetic.main.activity_sign_in.*

/**
 * SignInActivity
 *
 * @author Aditi Shirsat
 */
class ActivitySignIn : BaseActivity(), SignInContract.View {

    private lateinit var mPresenter: SignInPresenter

    private val mSignInData: MutableLiveData<Any> = MutableLiveData()

    private var password: String = ""
    private var username: String = ""

    override fun setPresenter() {
        mPresenter = SignInPresenter(this)
    }

    override fun getPresenter(): BasePresenter? {
        return mPresenter
    }


    override fun getLayout(): Int {
        return R.layout.activity_sign_in
    }

    override fun setListener() {
        btn_signIn.setOnClickListener {
            username = et_email.text.toString()
            password = et_password.text.toString()
            mPresenter.signIn(mSignInData, username, password)
        }

        tv_getStarted.setOnClickListener { startActivity<ActivitySignUp>() }
    }


    override fun setObservers() {
        mSignInData.observe(this, Observer { signInResponse ->
            when (signInResponse) {
                is String -> {
                    Utility.showSnackBar(rl_main, signInResponse)
                }
                is UserData -> {
                    UserData.setInstance(signInResponse)
                    startActivity<ActivityChooseLocation>()
                    finishAffinity()
                }
                is Boolean -> {
                    if (signInResponse) {
                        btn_signIn.hide()
                        btnLoader.show()
                    } else {
                        btn_signIn.show()
                        btnLoader.hide()
                    }
                }
            }
        })
    }


    override fun init() {
        overridePendingTransition(
            R.anim.animation_slide_right_center,
            R.anim.animation_stable
        )

        setUpToolbar(
            getString(R.string.sign_in), false
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.animation_stable,
            R.anim.animation_slide_center_right
        )
    }
}
