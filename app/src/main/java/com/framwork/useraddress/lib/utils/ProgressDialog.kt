package com.framwork.useraddress.lib.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import com.framwork.useraddress.R
import kotlinx.android.synthetic.main.dialog_loader.*


/**
 * Custom progress dialog which is displayed during API calls.
 *
 * @author Aditi Shirsat
 */
class ProgressDialog(context: Context) : Dialog(context, R.style.MyDialogTheme) {

    init {
        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        this.setContentView(R.layout.dialog_loader)
        indicator.setIndicatorColor(ContextCompat.getColor(context, R.color.white))
        indicator.setIndicator("BallClipRotatePulseIndicator")
        this.setCancelable(false)
    }

    companion object {

        private var progressDialog: ProgressDialog? = null

        /**
         * Display progress dialog.
         *
         * @param mContext Context
         */
        fun showDialog(mContext: Activity) {
            if (progressDialog == null && !mContext.isFinishing) {
                progressDialog =
                    ProgressDialog(mContext)
                progressDialog?.show()
            }
        }

        /**
         * Hide progress dialog.
         *
         * @param mContext Context
         */
        fun dismissDialog(mContext: Activity) {
            if (progressDialog != null && progressDialog!!.isShowing && !mContext.isFinishing)
                progressDialog?.dismiss()
            progressDialog = null
        }
    }
}
