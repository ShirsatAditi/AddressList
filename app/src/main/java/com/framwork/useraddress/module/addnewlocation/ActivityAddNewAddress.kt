package com.framwork.useraddress.module.addnewlocation

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.BaseActivity
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.base.SuccessData
import com.framwork.useraddress.lib.utils.Utility
import com.framwork.useraddress.lib.utils.hide
import com.framwork.useraddress.lib.utils.show
import kotlinx.android.synthetic.main.activity_add_new_location.*

/**
 * AddNewLocationActivity
 *
 * @author Aditi Shirsat
 */
class ActivityAddNewAddress : BaseActivity(), AddNewAddressContract.View {

    private lateinit var mPresenter: AddNewAddressPresenter
    private val mAddAddressData: MutableLiveData<Any> = MutableLiveData()

    override fun setPresenter() {
        mPresenter = AddNewAddressPresenter(this)
    }

    override fun getPresenter(): BasePresenter? {
        return mPresenter
    }

    override fun getLayout(): Int {
        return R.layout.activity_add_new_location
    }

    override fun setListener() {
        btn_save.setOnClickListener {
            mPresenter.addAddress(
                mAddAddressData,
                et_address_line1.text.toString(),
                et_address_line2.text.toString(),
                et_city.text.toString(),
                et_pinCode.text.toString()
            )
        }
    }

    override fun setObservers() {
        mAddAddressData.observe(this, Observer { mAddAddressResponse ->
            when (mAddAddressResponse) {
                is SuccessData -> {
                    setResult(Activity.RESULT_OK, Intent())
                    onBackPressed()
                }
                is String -> {
                    Utility.showSnackBar(rl_main, mAddAddressResponse)
                }
                is Boolean -> {
                    if (mAddAddressResponse) {
                        btn_save.hide()
                        btnLoader.show()
                    } else {
                        btn_save.show()
                        btnLoader.hide()
                    }
                }
            }
        })
    }

    override fun init() {
        overridePendingTransition(R.anim.animation_slide_right_center, R.anim.animation_stable)

        setUpToolbar(
            getString(R.string.add_new_address)
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