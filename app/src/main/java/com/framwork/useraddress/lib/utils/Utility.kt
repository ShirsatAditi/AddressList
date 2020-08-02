package com.framwork.useraddress.lib.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.Display
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.framwork.useraddress.BuildConfig
import com.framwork.useraddress.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.framwork.useraddress.lib.application.MainApplication
import java.io.*
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A singleton object containing all commonly used functions.
 *
 * @author Aditi Shirsat
 */
object Utility {

    /**
     * Check if device is connected to an active network.
     *
     * @return True if device is connected to an active network
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager: ConnectivityManager =
            MainApplication.instance.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * Hides keyboard from an activity.
     */
    fun hideKeyboard(activity: Activity) {
        val inputManager: InputMethodManager =
            MainApplication.instance.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView: View? = activity.currentFocus
        focusedView?.let {
            inputManager.hideSoftInputFromWindow(
                focusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /**
     * Store string in preferences file.
     *
     * @param key   Preferences Key
     * @param value String to be stored
     */
    fun addPreference(key: String, value: String) {
        val editor = MainApplication.instance.getContext().getSharedPreferences(
            MainApplication.instance.getContext().getString(R.string.app_name),
            MODE_PRIVATE
        ).edit()
        editor.putString(key, value)
        editor.apply()
        editor.commit()
    }

    /**
     * Remove string from preferences file.
     *
     * @param key Preferences Key
     */
    fun removePreference(key: String) {
        val editor = MainApplication.instance.getContext().getSharedPreferences(
            MainApplication.instance.getContext().getString(R.string.app_name),
            MODE_PRIVATE
        ).edit()
        editor.remove(key)
        editor.apply()
        editor.commit()
    }

    /**
     * Retrieve string from preferences file.
     *
     * @param key Preferences Key
     * @return String from preferences file based on Key
     */
    fun getPreference(key: String): String {
        val prefs = MainApplication.instance.getContext().getSharedPreferences(
            MainApplication.instance.getContext().getString(R.string.app_name),
            MODE_PRIVATE
        )
        return prefs.getString(key, "") ?: ""
    }

    /**
     * Check if the string is blank.
     *
     * @param text String
     * @return True if the String is blank
     */
    fun isBlankString(text: String): Boolean {
        return text.isBlank()
    }

    /**
     * Check if the string is digits only.
     *
     * @param text String
     * @return True if the String is digits only
     */
  /*  fun isDigitOnly(text: String): Boolean {
        return text.isDigitsOnly()
    }*/

    /**
     * Check if the email is Invalid.
     *
     * @param email Email
     * @return True if the Email is Invalid
     */
    fun isEmailInvalid(email: String): Boolean {
        return !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Check if the url is valid.
     *
     * @param url Url
     * @return True if the Url is valid
     */
    fun isUrlValid(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    /**
     * Vibrates device.
     *
     */
    @Suppress("DEPRECATION")
    fun vibrate() {
        val v =
            MainApplication.instance.getContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        else
            v.vibrate(150)
    }

    /**
     * Check if the device has single or multiple permissions.
     *
     * @param activity       Activity Context
     * @param permissionName Array of permission name to check
     * @param request_code   Unique request code to handel onPermissionResult
     * @return True if all permissions are granted
     */
    fun checkPermissions(
        activity: Activity,
        permissionName: Array<String>,
        request_code: Int
    ): Boolean {
        val listPermissionsNeeded = ArrayList<String>()
        for (i in permissionName) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    i
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionsNeeded.add(i)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                listPermissionsNeeded.toTypedArray(),
                request_code
            )
            return false
        }
        return true
    }

    /**
     * Display snackBar to forcefully grant permission from user.
     *
     * @param activity Activity Context
     * @param rootView Root View
     * @param msg      Message to be displayed
     */
    fun grantPermission(activity: Activity, rootView: View, msg: String) {
        val snackBar = Snackbar.make(rootView, msg, Snackbar.LENGTH_INDEFINITE)
        snackBar.view.setBackgroundColor(
            ContextCompat.getColor(
                rootView.context,
                R.color.colorPrimary
            )
        )
        snackBar.setActionTextColor(Color.WHITE)
        snackBar.setAction(activity.getString(R.string.settings)) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            activity.startActivity(intent)
        }
        snackBar.show()
        vibrate()
    }

    /**
     * Display normal snackBar.
     *
     * @param rootView Root View
     * @param msg      Message to be displayed
     */
    fun showSnackBar(rootView: View, msg: String) {
        val snackBar = Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(
            ContextCompat.getColor(
                rootView.context,
                R.color.colorPrimary
            )
        )
        snackBar.setActionTextColor(Color.WHITE)
        snackBar.show()
        vibrate()
    }

    /**
     * Display snackBar with an action button.
     *
     * @param rootView Root View
     * @param msg      Message to be displayed
     */
    fun showSnackBarWithAction(rootView: View, msg: String) {
        val snackBar = Snackbar.make(rootView, msg, Snackbar.LENGTH_INDEFINITE)
        snackBar.view.setBackgroundColor(
            ContextCompat.getColor(
                rootView.context,
                R.color.colorPrimary
            )
        )
        snackBar.setActionTextColor(Color.WHITE)
        snackBar.setAction("OK") { snackBar.dismiss() }
        snackBar.show()
        vibrate()
    }

    /**
     * Display toast to user.
     *
     * @param msg message to be displayed
     */
    fun showToast(msg: String) {
        Toast.makeText(MainApplication.instance.getContext(), msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Retrieve current date and time.
     *
     * @return current date and time in yyyy-MM-dd HH:mm:ss format
     */
    fun getDateTime(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        ).format(Calendar.getInstance().time)
    }

    fun getDateInRequiredFormat(sourcePattern: String, destPattern: String, date: String): String {

        val datee = SimpleDateFormat(sourcePattern).parse(date)
        return SimpleDateFormat(
            destPattern,
            Locale.US
        ).format(datee)
    }


    /**
     * Retrieve current timestamp.
     *
     * @return current timestamp in Linux supported format
     */
    fun getTimeStamp(): String {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
    }

    /**
     * Prints message in logcat.
     * Only for debug mode.
     *
     * @param msg message to be printed
     */
    fun printLog(msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(MainApplication.instance.getContext().getString(R.string.app_name), "SSM : $msg")
    }


    /**
     * Display toast to user.
     * Only for debug mode.
     *
     * @param msg message to be displayed
     */
    fun showDebugToast(msg: String) {
        if (BuildConfig.DEBUG)
            Toast.makeText(MainApplication.instance.getContext(), msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Display forced logout alert to user.
     *
     * @param activity Activity Context
     */
    fun showForcedLogoutAlert(activity: Activity) {
        /*  val builder = AlertDialog.Builder(activity)
          builder.setIcon(R.mipmap.ic_launcher)
          builder.setTitle(R.string.app_name)
          builder.setCancelable(false)
          builder.setMessage(activity.getString(R.string.force_logout_msg))
          builder.setPositiveButton(activity.getString(R.string.logout)) { _, _ -> activity.finishAffinity() }

          if (!activity.isFinishing) {
              val dialog = builder.create()
              dialog.show()
              vibrate()
          }*/
    }

    /**
     * Converts milliseconds to time.
     *
     * @param millis Milliseconds
     * @return Time in HH:MM:SS format
     */
    fun convertMillisToTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(millis)
    }

    /**
     * Converts time to milliseconds .
     *
     * @param time Time
     * @return Milliseconds
     */
    fun convertTimeToMillis(time: String): Long {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.parse(time).time
    }

    /**
     * Check if external storage is writable.
     *
     * @return True if external storage is writable
     */
    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    /**
     * Check if file is created and contains some data .
     *
     * @param fileName File name to check
     * @return True if file is created and contains some data
     */
    fun isFileCreated(fileName: String): Boolean {
        val file = File(MainApplication.instance.getContext().filesDir, fileName)
        return file.exists() && file.length() > 0
    }

    /**
     * Save file in device private internal storage.
     *
     * @param fileName The name of the file to be saved
     * @param model    Data to be stored in the file
     */
    fun saveFileInStorage(fileName: String, model: Any) {
        try {
            val file = File(MainApplication.instance.getContext().filesDir, fileName)
            val fos = MainApplication.instance.getContext().openFileOutput(file.name, MODE_PRIVATE)
            fos.write(Gson().toJson(model).toByteArray())
            fos.close()
        } catch (io: IOException) {
            io.printStackTrace()
        }
    }

    /**
     * Retrieve data from a file.
     *
     * @param fileName The name of the file to be retrieved
     * @return Data store in the file in string
     */
    fun retrieveFileContent(fileName: String): String {
        val file = File(MainApplication.instance.getContext().filesDir, fileName)

        val bytes = ByteArray(file.length().toInt())

        try {
            val inputStream = FileInputStream(file)
            inputStream.read(bytes)
            inputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return String(bytes)
    }

    /**
     * Delete file from device private internal storage.
     *
     * @param fileName The name of the file to be deleted
     */
    fun deleteFile(fileName: String) {
        val file = File(MainApplication.instance.getContext().filesDir, fileName)
        if (file.exists())
            file.delete()
    }

    /**
     * Sets apps locale to support localisation.
     *
     */
    @Suppress("DEPRECATION")
    fun localisation(activity: Context) {
        val languageKey: String = "en"

        val locale = Locale(languageKey)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    /**
     * Check if the application is installed.
     *
     * @param packageName Application package name to check
     * @return True if application is installed
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            MainApplication.instance.getContext().packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Check if the app is installed from play store.
     *
     * @return True if app is downloaded from play store
     */
    fun isAppVerified(): Boolean {
        val validInstallers =
            ArrayList(listOf("com.android.vending", "com.google.android.feedback"))
        val installer =
            MainApplication.instance.getContext()
                .packageManager.getInstallerPackageName(BuildConfig.APPLICATION_ID)
        return installer != null && validInstallers.contains(installer)
    }

    /**
     * Generate unique ID for notifications.
     *
     * @return Unique ID in mmssSS format
     */
    fun generateUniqueId(): Int {
        val sdf = SimpleDateFormat("mmssSS", Locale.US)
        return Integer.parseInt(sdf.format(Calendar.getInstance().time))
    }

    /**
     * Get unique android device id.
     * Note: This id gets changed when the device is factory restored.
     *
     * @return unique android device id.
     */
    @SuppressLint("HardwareIds")
    fun getDeviceUniqueId(): String {
        return Settings.Secure.getString(
            MainApplication.instance.getContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    /**
     * Calculate dynamic Image View height based on aspect ratio.
     *
     * @param activity Activity Context
     * @param height   Image height
     * @param width    Image width
     * @return height calculated based on aspect ratio
     */
    fun calculateImageViewHeight(activity: Activity, height: String, width: String): Int {

        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return (displayMetrics.widthPixels * height.toFloat() / width.toFloat()).toInt()
    }

    /**
     * Load image using glide.
     *
     * @param iv  ImageView
     * @param url url to load
     */
    fun setImageResource(iv: ImageView, url: String) {
        Glide.with(MainApplication.instance.getContext())
            .load(url)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_foreground).error(R.drawable.ic_launcher_foreground))
            .into(iv)
    }

    /**
     * Check if the string length is smaller than the provided length.
     *
     * @param string String to check
     * @param length Length to check
     * @return True if length of string is less than provided Length
     */
    fun isSmallerThan(string: String, length: Int): Boolean {
        return string.length < length
    }

    /**
     * Check if the password is invalid.
     *
     * @param string String to check
     * @return True if the password is invalid
     */
    fun isPasswordInvalid(string: String): Boolean {
        val passwordPattern = Pattern.compile("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")
        return !passwordPattern.matcher(string).matches()
    }

    fun isPasswordInvalidWithoutSymbol(string: String): Boolean {
        val passwordPattern = Pattern.compile("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[a-z]).{8,16}$")
        return !passwordPattern.matcher(string).matches()
    }

    /**
     * Get device screen size in inches.
     *
     * @param activity Activity Context
     * @return Screen size of the device in inches
     */
    fun getDeviceScreenSize(activity: Activity): String {
        var x = 0.0
        var y = 0.0
        val mWidthPixels: Int
        val mHeightPixels: Int
        try {
            val windowManager = activity.windowManager
            val display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val realSize = Point()
            Display::class.java.getMethod("getRealSize", Point::class.java)
                .invoke(display, realSize)
            mWidthPixels = realSize.x
            mHeightPixels = realSize.y
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            x = (mWidthPixels / dm.xdpi).toDouble().pow(2.0)
            y = (mHeightPixels / dm.ydpi).toDouble().pow(2.0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return String.format(Locale.US, "%.1f", sqrt(x + y))
    }

    /**
     * Check if device screen is Xlarge
     *
     * @return True if device screen is Xlarge
     */
    fun isXLargeScreen(): Boolean {
        return MainApplication.instance.getContext().resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
    }

    /**
     * Check if the device is tablet.
     *
     * @return True if device is a tablet
     */
    fun isTablet(): Boolean {
        val dm = MainApplication.instance.getContext().resources.displayMetrics
        val screenWidth = dm.widthPixels / dm.xdpi
        val screenHeight = dm.heightPixels / dm.ydpi
        val size = sqrt(screenWidth.toDouble().pow(2.0) + screenHeight.toDouble().pow(2.0))
        return size >= 7
    }


    /**
     * Convert image to base64
     *
     * @param filePath Image file path
     * @return base64 string
     */
    fun ConvertImageToBase64(filePath: String): String {
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(filePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val bytes: ByteArray
        val buffer = ByteArray(10240)
        val bytesRead: Int
        val output = ByteArrayOutputStream()
        try {
            bytesRead = inputStream!!.read(buffer)
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        bytes = output.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    /**
     * Displays date picker dialog.
     *
     * @param activity          Activity Context
     * @param textView          TextView
     * @param format            Date Format
     * @param isShowPastDates   True if should display past dates
     * @param isShowFutureDates True if should display future dates
     */
    fun showDatePicker(
        activity: Activity,
        textView: TextView,
        format: String,
        isShowPastDates: Boolean,
        isShowFutureDates: Boolean
    ) {
        hideKeyboard(activity)
        Locale.setDefault(Locale.ENGLISH)
        val mCalendar = Calendar.getInstance()

        val datePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            mCalendar.set(Calendar.YEAR, year)
            mCalendar.set(Calendar.MONTH, monthOfYear)
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            textView.text = SimpleDateFormat(format, Locale.getDefault()).format(mCalendar.time)
        }

        /*Set selected date in date picker if applicable*/
        val selectedDate = textView.text.toString().trim()
        if (!isBlankString(selectedDate)) {
            try {
                mCalendar.timeInMillis =
                    SimpleDateFormat(format, Locale.getDefault()).parse(selectedDate).time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        val mDate = DatePickerDialog(
            activity,
            datePicker,
            mCalendar.get(Calendar.YEAR),
            mCalendar.get(Calendar.MONTH),
            mCalendar.get(Calendar.DAY_OF_MONTH)
        )

        val currentDate = System.currentTimeMillis() - 1000 // Current date
        if (!isShowPastDates) {
            //Disable past dates
            mDate.datePicker.minDate = currentDate
        }
        if (!isShowFutureDates) {
            //Disable future dates
            mDate.datePicker.maxDate = currentDate
        }

        mDate.setCanceledOnTouchOutside(false)
        mDate.show()
    }

    /**
     * Displays time picker dialog.
     *
     * @param activity     Activity Context
     * @param textView     TextView
     * @param is24HourView True=Display time picker in 24 hour format, False=Display time picker in 12 hour format
     */
    fun showTimePicker(
        activity: Activity,
        textView: TextView,
        is24HourView: Boolean
    ) {
        hideKeyboard(activity)
        Locale.setDefault(Locale.ENGLISH)
        val mCalendar = Calendar.getInstance()
        val format = if (is24HourView) "HH:mm" else "hh:mm aaa"

        val timePicker = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            mCalendar.set(Calendar.HOUR_OF_DAY, hour)
            mCalendar.set(Calendar.MINUTE, minute)
            textView.text = SimpleDateFormat(format, Locale.US).format(mCalendar.time)
        }

        /*Set selected time in time picker if applicable*/
        val selectedTime = textView.text.toString().trim()
        if (!isBlankString(selectedTime)) {
            try {
                mCalendar.timeInMillis =
                    SimpleDateFormat(format, Locale.US).parse(selectedTime).time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        TimePickerDialog(
            activity,
            timePicker,
            mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            is24HourView
        ).show()
    }

    /**
     * Display error messages on TextInputLayout.
     *
     * @param textInputLayout TextInputLayout
     * @param editText        EditText
     * @param msg             Error message to display
     */
    fun materialValidator(textInputLayout: TextInputLayout, editText: EditText, msg: String) {
        textInputLayout.isErrorEnabled = true
        textInputLayout.error = msg
        editText.requestFocus()

        //TextWatcher to remove error when user starts typing in EditText.
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (editText.text.isNotEmpty()) {
                    editText.error = null
                    textInputLayout.isErrorEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    /**
     * Compress image and preserve image rotation.
     *
     * @param imagePath Path of the image to compress
     * @return Compressed image file
     */
    fun getCompressedFile(imagePath: String): File {
        val file = File(imagePath)
        val length = file.length() / 1024 // Size in KB
        if (length > 600) {
            try {
                val exifOrientation =
                    ExifInterface(file.absolutePath).getAttribute(ExifInterface.TAG_ORIENTATION)

                BitmapFactory.decodeFile(file.path)
                    .compress(Bitmap.CompressFormat.JPEG, 50, FileOutputStream(file))

                if (exifOrientation != null) {
                    val newExif = ExifInterface(file.absolutePath)
                    newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
                    newExif.saveAttributes()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        return file
    }

    /**
     * @param date any date string
     *
     * @return return date in format [hh:mm:ss]
     * */

    fun getServerFormattedDate(date: String?): String {
        if (date != null && !isBlankString(date)) {
            var formattedDate: Date? = null

            try {
                formattedDate =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (formattedDate != null)
                return SimpleDateFormat("MMM dd", Locale.getDefault()).format(formattedDate)
        }
        return ""
    }

    /**
     * @param mContext context of class
     * @param image image to download
     * @param callbackImage callback to when image is ready
     *
     * @return return date in format [hh:mm:ss]
     * */

    fun getBitmapFromImageURL(
        mContext: Context,
        image: String,
        callbackImage: CallbackImage
    ) {

        Glide.with(mContext)
            .asBitmap()
            .load(image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    var s = "image"
                    val i = image.lastIndexOf('/')
                    if (i >= 0) {
                        s = image.substring(i + 1)
                    }

                    val mediaStorageDir = File(
                        Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        mContext.resources.getString(R.string.app_name)
                    )

                    val imgFile = File(mediaStorageDir, s)

                    if (mediaStorageDir.exists()) {

                        if (File(mediaStorageDir, s).exists()) {
                            callbackImage.imageReady(Uri.parse(Uri.fromFile(imgFile).toString()))
                        } else {
                            try {
                                val fos = FileOutputStream(imgFile, false)
                                resource.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                                fos.close()
                            } catch (e: Exception) {
                                Log.e("SAVE_IMAGE", e.message, e)
                            }

                            callbackImage.imageReady(Uri.fromFile(imgFile))
                        }
                    } else {
                        try {
                            mediaStorageDir.mkdirs()
                            val fos = FileOutputStream(imgFile)
                            resource.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                            fos.close()
                        } catch (e: Exception) {
                            Log.e("SAVE_IMAGE", e.message, e)
                        }

                        callbackImage.imageReady(Uri.fromFile(imgFile))
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    fun getDeviceLanguageId(): String? {
        val lang = Locale.getDefault().displayLanguage
        return if (lang.equals("العربية", ignoreCase = true)) {
            Constants.LANGUAGE_AREBIC
        } else if (lang.equals("English", ignoreCase = true)) {
            Constants.LANGUAGE_ENGLISH
        } else {
            Constants.LANGUAGE_ENGLISH
        }
    }

    fun encodeEmoji(message: String): String {
        return try {
            URLEncoder.encode(
                message,
                "UTF-8"
            )
        } catch (e: UnsupportedEncodingException) {
            message
        }
    }
}

interface CallbackImage {
    fun imageReady(bitmapUri: Uri?)
}

interface CallbackVideo {
    fun videoCompressed(
        mImageUri: Uri, mPosition: Int
    )
}