package com.framwork.useraddress.lib.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.framwork.useraddress.BuildConfig
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.application.MainApplication
import com.framwork.useraddress.lib.dependencyinjection.APIComponent
import com.framwork.useraddress.lib.utils.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.io.File


/**
 * @author Aditi Shirsat
 */
abstract class BaseActivity : AppCompatActivity(), BaseView {
    companion object {
        lateinit var mDestination: File
        const val REQUEST_CAMERA_PERMISSION = 101
        const val REQUEST_GALLERY_PERMISSION = 102
        const val REQUEST_CAMERA_IMAGE = 1
        const val REQUEST_GALLERY_IMAGE = 2
        const val REQUEST_CAMERA_VIDEO = 3
        const val REQUEST_GALLERY_VIDEO = 4
    }

    private lateinit var apiComponent: APIComponent
    private lateinit var mLoadingDialog: Dialog
    private lateinit var mLoadingView: View
    private var rootview: View? = null
    private var toolbar: TextView? = null
    private lateinit var menuItemLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())

        apiComponent = (application as MainApplication).getAPIComponent()

        rootview = findViewById(R.id.rootview)
        if (rootview == null)
            showDebugToast("rootview not set.")

        setPresenter()
        init()
        setObservers()
        setListener()
    }

    override fun onResume() {
        super.onResume()
        localisation()
    }

    override fun onRestart() {
        super.onRestart()
        localisation()
    }

    abstract fun setPresenter()


    abstract fun getPresenter(): BasePresenter?

    /**
     * abstract function to get layout id
     */
    @LayoutRes
    abstract fun getLayout(): Int

    /**
     * abstract function for Listener(ClickListener)
     */
    abstract fun setListener()

    /**
     * abstract function for set Observers on live data
     */
    abstract fun setObservers()

    /**
     * Abstract function for initializing.
     */
    abstract fun init()

    /**
     * Setup toolbar elements.
     *
     * @param title Title
     * @param showBackButton True if display back button
     */
    fun setUpToolbar(
        title: String,
        showBackButton: Boolean = false
    ) {
        tv_toolbar_title.text = title
        if (showBackButton) ll_back.show() else ll_back.hide()
        ll_back.setOnClickListener { onBackPressed() }
    }

    override fun showDebugToast(message: String) {
        if (BuildConfig.DEBUG) {
            //            Toast.makeText(this, getClass().getSimpleName() + "  " + message, Toast.LENGTH_LONG).show();
            //Toast.makeText(this,  message, Toast.LENGTH_LONG).show();

            Log.i("DEBUG", message)
        }
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showSnackbar(message: String) {
        val snack = Snackbar.make(this.rootview!!, message, Snackbar.LENGTH_LONG)
        snack.view.setBackgroundColor(
            ContextCompat.getColor(
                this.context(true),
                R.color.colorPrimary
            )
        )
        val textView: TextView =
            snack.view.findViewById(com.google.android.material.R.id.snackbar_text)
        textView.typeface = ResourcesCompat.getFont(this.context(true), R.font.ubuntu_medium)
        snack.show()

        Utility.vibrate()
    }

    protected fun showSnackBarWithAction(
        message: String,
        buttonText: String,
        mListener: View.OnClickListener
    ) {
        val snack = Snackbar.make(this.rootview!!, message, Snackbar.LENGTH_INDEFINITE)
        snack.setAction(buttonText) {
            mListener.onClick(it)
        }
        snack.show()
    }


    override fun showMaterialProgress() {

        mLoadingDialog = Dialog(this)
        mLoadingDialog.window
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mLoadingView =
            (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.loadingview,
                null,
                false
            )
        (mLoadingView as LottieAnimationView).scale = 0.3f
        mLoadingDialog.setContentView(mLoadingView)
        mLoadingDialog.setCancelable(false)
        mLoadingDialog.setOnKeyListener { _: DialogInterface?, keyCode: Int, _: KeyEvent? ->

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mLoadingDialog.dismiss()
                //finish();
            }
            true
        }

        mLoadingDialog.setCancelable(false)
        mLoadingDialog.show()
    }

    override fun hideMaterialProgress() {
        if (::mLoadingDialog.isInitialized)
            mLoadingDialog.dismiss()
    }

    fun addFragmentFromBottomNavigation(fragment: BaseFragment, extras: Bundle?) {
        if (extras != null)
            fragment.arguments = extras

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
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
                REQUEST_CAMERA_PERMISSION
            )
        ) {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            mDestination =
                File(
                    activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    System.currentTimeMillis().toString() + ".jpg"
                )
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mDestination))
            startActivityForResult(takePicture, REQUEST_CAMERA_IMAGE)
        }
    }

    /**
     * Open gallery to choose photo.
     */
    fun openGallery(activity: Activity) {
        if (Utility.checkPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_GALLERY_PERMISSION
            )
        ) {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE)
        }
    }

    //change language of system
    open fun localisation() {
      /*  var languageKey = ""
        languageKey = if (Constants.getUserData() != null) {
            Constants.getUserData().language_preference ?: Constants.LANGUAGE_ENGLISH
        } else {
            Utility.getDeviceLanguageId() ?: Constants.LANGUAGE_ENGLISH
        }

        val locale = Locale(languageKey)
        Locale.setDefault(locale)
        val res = resources
        val config =
            Configuration(res.configuration)
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
        if (languageKey == Constants.LANGUAGE_AREBIC) {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }*/
    }


//    override fun getServerErrorMsg(errorCode: String): String {
//        return Constants.getServerErrorMsg(errorCode)
//    }

    override fun getAPIComponent(): APIComponent {
        return apiComponent
    }

    override fun onNoNetworkFoud() {

    }

    override fun onForceLogout() {
    }

    override fun onServerError() {
    }

    override fun showSuccessOrErrorMessage(
        dialogTitle: String,
        dialogMessage: String,
        positiveText: String,
        negativeText: String,
        positiveCallback: View.OnClickListener?,
        negativeCallBack: View.OnClickListener?
    ) {
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
}