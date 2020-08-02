package com.framwork.useraddress.module.onboarding.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.BaseActivity
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.utils.*
import com.framwork.useraddress.model.UserData
import com.framwork.useraddress.module.onboarding.signin.ActivitySignIn
import com.framwork.useraddress.module.chooselocation.ActivityChooseLocation
import kotlinx.android.synthetic.main.activity_sign_up.*


/**
 * SignUpActivity
 *
 * @author Aditi Shirsat
 */
class ActivitySignUp : BaseActivity(), SignUpContract.View {
    private lateinit var mPresenter: SignUpPresenter
    private val mSignUpData: MutableLiveData<Any> = MutableLiveData()

    override fun setPresenter() {
        mPresenter = SignUpPresenter(this)
    }

    override fun getPresenter(): BasePresenter? {
        return mPresenter
    }

    override fun getLayout(): Int {
        return R.layout.activity_sign_up
    }

    override fun setListener() {
        btn_sign_up.setOnClickListener {
            val email = et_email.text.toString()
            val username = et_username.text.toString()
            val password = et_password.text.toString()

            setEditable(false)
            mPresenter.signUp(
                mSignUpData,
                username,
                email,
                password
            )
        }

        tv_signIn.setOnClickListener {
            startActivity<ActivitySignIn>()
            finishAffinity()
        }
    }

    override fun setObservers() {
        mSignUpData.observe(this, Observer { mSignUpResponse ->
            when (mSignUpResponse) {
                is UserData -> {
                    Constants.setUserId(mSignUpResponse.userData?.user_id?:"")
                    startActivity<ActivityChooseLocation>()
                    finish()
                }
                is String -> {
                    setEditable(true)
                    Utility.showSnackBar(rl_main, mSignUpResponse)
                }
                is Boolean -> {
                    if (mSignUpResponse) {
                        btn_sign_up.hide()
                        btnLoader.show()
                    } else {
                        btn_sign_up.show()
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
            getString(R.string.get_started)
        )
    }

    /**
     *
     * @param isEditable true/false to make edit text editable true and false
     *
     * This method is used to make edit text editable false during api call
    * */

    private fun setEditable(isEditable: Boolean) {
        if (isEditable) {

            et_email.isFocusable = true
            et_email.isFocusableInTouchMode = true
            et_email.requestFocus()
            et_email.setSelection(et_email.text.length)

            et_username.isFocusable = true
            et_username.isFocusableInTouchMode = true
            et_username.requestFocus()
            et_username.setSelection(et_username.text?.length ?: 0)

            et_password.isFocusable = true
            et_password.isFocusableInTouchMode = true
            et_password.requestFocus()
            et_password.setSelection(et_password.text.length)
        } else {
            et_username.isFocusable = false
            et_username.isFocusableInTouchMode = false

            et_email.isFocusable = false
            et_email.isFocusableInTouchMode = false

            et_password.isFocusable = false
            et_password.isFocusableInTouchMode = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.animation_stable,
            R.anim.animation_slide_center_right
        )
    }
}
