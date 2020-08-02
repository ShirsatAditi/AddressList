package com.framwork.useraddress.lib.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.framwork.useraddress.BuildConfig
import com.framwork.useraddress.lib.dependencyinjection.APIComponent
import com.framwork.useraddress.lib.utils.Utility
import com.google.android.material.snackbar.Snackbar
import java.io.File

/**
 * @author Aditi Shirsat
 */
abstract class BaseFragment : Fragment(), BaseView {

    companion object {
        val TAG = BaseActivity::class.simpleName
    }

    private lateinit var mLoadingView: View
    private lateinit var mLoadingDialog: Dialog
    private lateinit var mBaseActivity: BaseActivity
    private lateinit var apiComponent: APIComponent
    private lateinit var rootview: View
    private lateinit var toolbar: TextView
    private lateinit var menuItemLayout: LinearLayout
    lateinit var itemView: View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true
        //mFragmentToolbar = returnView.findViewById(R.id.toolbar) as Toolbar
        //checkOptionsMenu()
        itemView = inflater.inflate(getLayout(), container, false)
        setObservers()
        setPresenter()
        setListener()
        apiComponent = getBaseActivity()!!.getAPIComponent()
        init()
        rootview = itemView.rootView

        return itemView
    }

    override fun onResume() {
        super.onResume()
        localisation()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as BaseActivity
        localisation()
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, "Fragment detached from activity")
    }

    open fun onToolbarTitleClick() {

    }

    open fun onRightOptionsItemClick() {

    }

    open fun onLeftOptionsItemClick() {

    }

    abstract fun setPresenter()
    abstract fun getPresenter(): BasePresenter?


    @LayoutRes
    abstract fun getLayout(): Int

    /**
     * Abstract function to setup observers.
     */
    abstract fun setObservers()

    /**
     * Abstract function to setup click listeners.
     */
    abstract fun setListener()

    /**
     * Abstract function for initializing.
     */
    abstract fun init()

    override fun showDebugToast(message: String) {
        if (BuildConfig.DEBUG) {
            //            Toast.makeText(this, getClass().getSimpleName() + "  " + message, Toast.LENGTH_LONG).show();
            //Toast.makeText(this,  message, Toast.LENGTH_LONG).show();

            Log.i("DEBUG", message)
        }
    }

    override fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    protected fun showSnackbar(message: String) {
        if (getBaseActivity() != null) getBaseActivity()!!.showSnackbar(message)
    }

    protected fun showSnackBarWithAction(
        message: String,
        buttonText: String,
        mListener: View.OnClickListener
    ) {
        val snack = Snackbar.make(rootview, message, Snackbar.LENGTH_INDEFINITE)
        snack.setAction(buttonText) {
            mListener.onClick(it)
        }
        snack.show()
    }

    override fun showMaterialProgress() {
        if (getBaseActivity() != null) getBaseActivity()!!.showMaterialProgress()
    }

//    override fun getLocalErrorMsg(key: String): String {
//        return Constants.getLocalErrorMsg(key)
//    }

//    override fun getServerErrorMsg(errorCode: String): String {
////        return Constants.getServerErrorMsg(errorCode)
//    }

    override fun hideMaterialProgress() {
        if (getBaseActivity() != null) getBaseActivity()!!.hideMaterialProgress()
    }

    override fun showSuccessOrErrorMessage(
        dialogTitle: String,
        dialogMessage: String,
        positiveText: String,
        negativeText: String,
        positiveCallback: View.OnClickListener?,
        negativeCallBack: View.OnClickListener?
    ) {
        getBaseActivity()?.showSuccessOrErrorMessage(
            dialogTitle, dialogMessage,
            positiveText, negativeText,
            positiveCallback, negativeCallBack
        )
    }

    /**
     * Open camera to take photo.
     */
    fun openCamera(activity: Activity) {
        if (Utility.checkPermissions(
                activity,
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                BaseActivity.REQUEST_CAMERA_PERMISSION
            )
        ) {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            BaseActivity.mDestination =
                File(
                    activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    System.currentTimeMillis().toString() + ".jpg"
                )
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(BaseActivity.mDestination))
            startActivityForResult(takePicture, BaseActivity.REQUEST_CAMERA_IMAGE)
        }
    }

    /**
     * Open gallery to choose photo.
     */
    fun openGallery(activity: Activity) {
        if (Utility.checkPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                BaseActivity.REQUEST_GALLERY_PERMISSION
            )
        ) {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, BaseActivity.REQUEST_GALLERY_IMAGE)
        }
    }

    private fun localisation() {
        getBaseActivity()!!.localisation()
    }

    override fun getAPIComponent(): APIComponent {
        return apiComponent
    }

    override fun onForceLogout() {
        getBaseActivity()?.onForceLogout()
    }

    override fun onNoNetworkFoud() {
        getBaseActivity()?.onNoNetworkFoud()
    }

    override fun onServerError() {
        getBaseActivity()?.onServerError()
    }

    fun getBaseActivity(): BaseActivity? {
        if (mBaseActivity == null) {
            if (getAPIComponent() != null)
                mBaseActivity = getAPIComponent().app.getCurrentForegroundActivity()
            else
                Log.i(TAG, "getBaseActivity: getApiComponent() is null")
        }
        return mBaseActivity
    }

    override fun onStop() {
        super.onStop()
        if (getPresenter() != null)
            getPresenter()?.onStop()
    }

    override fun onPause() {
        super.onPause()
        if (getPresenter() != null)
            getPresenter()?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (getPresenter() != null)
            getPresenter()?.onDestroy()
    }
}