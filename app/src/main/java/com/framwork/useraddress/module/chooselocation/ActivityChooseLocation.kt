package com.framwork.useraddress.module.chooselocation

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.AddressData
import com.framwork.useraddress.lib.base.AddressList
import com.framwork.useraddress.lib.base.BaseActivity
import com.framwork.useraddress.lib.base.BasePresenter
import com.framwork.useraddress.lib.interfaces.EnumClicks
import com.framwork.useraddress.lib.interfaces.OnRecyclerClickListener
import com.framwork.useraddress.lib.utils.Utility
import com.framwork.useraddress.lib.utils.hide
import com.framwork.useraddress.lib.utils.show
import com.framwork.useraddress.module.addnewlocation.ActivityAddNewAddress
import kotlinx.android.synthetic.main.activity_choose_location.*


/**
 * ChooseLocationActivity
 *
 * @author Aditi Shirsat
 */
class ActivityChooseLocation : BaseActivity(), ChooseLocationContract.View,
    OnRecyclerClickListener {
    companion object {
        const val REQUEST_ADD_ADDRESS = 101
    }

    private lateinit var mPresenter: ChooseLocationPresenter
    private val mAddressData: MutableLiveData<Any> = MutableLiveData()
    private val mList: ArrayList<AddressList> = arrayListOf()
    private val mAdapter = ChooseLocationAdapter(mList, this)

    override fun setPresenter() {
        mPresenter = ChooseLocationPresenter(this)
    }

    override fun getPresenter(): BasePresenter? {
        return mPresenter
    }

    override fun getLayout(): Int {
        return R.layout.activity_choose_location
    }

    override fun setListener() {
        btn_add_new_location.setOnClickListener {
            startActivityForResult(
                Intent(this, ActivityAddNewAddress::class.java),
                REQUEST_ADD_ADDRESS
            )
        }
    }

    override fun setObservers() {
        mAddressData.observe(this, Observer { mAddressResponse ->
            when (mAddressResponse) {
                is AddressData -> {
                    mList.clear()
                    mList.addAll(mAddressResponse.addressList ?: arrayListOf())
                    mAdapter.notifyDataSetChanged()
                }
                is String -> {
                    Utility.showSnackBar(rootview, mAddressResponse)
                }
                is Boolean -> {
                    if (mAddressResponse) {
                        loader.show()
                        btn_add_new_location.hide()
                        cv_chooseLocation.hide()
                    } else {
                        loader.hide()
                        btn_add_new_location.show()
                        cv_chooseLocation.show()
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
            getString(R.string.select_location)
        )

        mPresenter.getAddress(mAddressData)

        rv_chooseLocation.layoutManager = LinearLayoutManager(this)
        rv_chooseLocation.adapter = mAdapter
    }

    override fun onRecyclerClick(where: EnumClicks, view: View, vararg position: Int) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_ADD_ADDRESS)
                mPresenter.getAddress(mAddressData)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.animation_stable,
            R.anim.animation_slide_center_right
        )
    }
}