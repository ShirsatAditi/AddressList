package com.framwork.useraddress.lib.base

import android.view.View
import androidx.annotation.Nullable
import com.framwork.useraddress.lib.dependencyinjection.APIComponent

/**
 * @author Aditi Shirsat
 */
interface BaseView {
    fun showDebugToast(message: String)
    fun showMaterialProgress()
    fun hideMaterialProgress()

    fun getAPIComponent(): APIComponent

    fun showToast(message: String)

    fun onNoNetworkFoud()

    fun onForceLogout()

    fun onServerError()

    fun showSuccessOrErrorMessage(
        dialogTitle: String,
        dialogMessage: String,
        positiveText: String,
        negativeText: String, @Nullable positiveCallback: View.OnClickListener?,
        @Nullable negativeCallBack: View.OnClickListener?
    )

}