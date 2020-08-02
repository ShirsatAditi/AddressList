package com.framwork.useraddress.lib.utils


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.framwork.useraddress.BuildConfig
import com.framwork.useraddress.R
import com.framwork.useraddress.lib.base.BaseActivity
import com.framwork.useraddress.lib.base.BaseFragment
import com.framwork.useraddress.lib.base.BaseView
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.lang3.StringEscapeUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*


/**
 * Extension method to display the view. (visibility = View.VISIBLE)
 */
fun View.show(): View {
    if (this != null && visibility != View.VISIBLE) visibility = View.VISIBLE
    return this
}

/**
 * Extension method to hide the view. (visibility = View.INVISIBLE)
 */
fun View.hide(): View {
    if (this != null && visibility != View.INVISIBLE) visibility = View.INVISIBLE
    return this
}

/**
 * Extension method to remove the view. (visibility = View.GONE)
 */
fun View.remove(): View {
    if (visibility != View.GONE) visibility = View.GONE
    return this
}

/**
 * Extension method to toggle a view's visibility.
 */
fun View.toggleVisibility(): View {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    return this
}

/**
 * Extension method to get color from resources.
 */
fun Context.getColorCompact(@ColorRes id: Int) = ContextCompat.getColor(this, id)

/**
 * Extension method to get drawable from resources.
 */
fun Context.getDrawableCompact(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

/**
 * Extension method to start an Activity.
 */
inline fun <reified T : Activity> Context?.startActivity() =
    this?.startActivity(Intent(this, T::class.java))

/**
 * Extension method to start an Activity for result.
 */
inline fun <reified T : Activity> Activity?.startActivityForResult(requestCode: Int) =
    this?.startActivityForResult(Intent(this, T::class.java), requestCode)

/**
 * Extension method to execute browse intent.
 */
fun Context.browseIntent(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Extension method to execute share intent.
 */
fun Context.shareIntent(text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Extension method to execute send email intent.
 */
fun Context.sendEmailIntent(subject: String = "", text: String = "", vararg email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, email)
            if (subject.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, subject)
            if (text.isNotBlank()) putExtra(Intent.EXTRA_TEXT, text)
        }
        intent.resolveActivity(packageManager)?.let { startActivity(intent) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Extension method to execute call intent.
 */
fun Context.makeCallIntent(number: String) {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Extension method to execute send SMS intent.
 */
fun Context.sendSmsIntent(number: String, text: String = "") {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$number")).apply {
            putExtra("sms_body", text)
        }
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Extension method to open PlayStore for rating the app.
 */
fun Context.rate() {
    try {
        browseIntent("market://details?id=$packageName")
    } catch (e: android.content.ActivityNotFoundException) {
        browseIntent("http://play.google.com/store/apps/details?id=$packageName")
    }
}

/**
 * Extension method to open PlayStore for forced update.
 */
fun Context.forcedUpdate() {
    try {
        browseIntent("market://details?id=$packageName")
    } catch (e: android.content.ActivityNotFoundException) {
        browseIntent("http://play.google.com/store/apps/details?id=$packageName")
    } finally {
        (this as Activity).finishAffinity()
    }
}

/**
 * Extension method to inflate layout for ViewGroup.
 */
fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

/**
 * Extension method to display underLine for TextView.
 */
fun TextView.underLine(underLine: Boolean) {
    this.paintFlags = if (underLine)
        this.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    else
        this.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
}

/**
 * Extension method to display strike through for TextView.
 */
fun TextView.strikeThrough(strikeThrough: Boolean) {
    this.paintFlags = if (strikeThrough)
        this.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    else
        this.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

/**
 * Extension method to set different color for substring in a TextView.
 */
fun TextView.setColorOfSubstring(substring: String, color: Int) {
    try {
        val spannable = android.text.SpannableString(text)
        val start = text.indexOf(substring)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, color)),
            start,
            start + substring.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = spannable
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Extension method to set font for TextView.
 */
fun TextView.setFont(@FontRes id: Int) {
    this.typeface = ResourcesCompat.getFont(context, id)
}

/**
 * Extension method to append 0 for single digit.
 */
fun Int.twoDigitTime() = if (this < 10) "0" + toString() else toString()

fun getTimeZone() :String{
    val tz = TimeZone.getDefault()
    return tz.getDisplayName(
        false,
        TimeZone.SHORT
    )
}

/**
 * Extension method to load image from an url for ImageView.
 */
fun ImageView.loadFromUrl(imageUrl: String?, placeHolder: Int = R.drawable.placeholder) {
    Glide.with(this)
        .load(imageUrl)
        .placeholder(placeHolder)
        .error(placeHolder).diskCacheStrategy(
        DiskCacheStrategy.AUTOMATIC).into(this)
}

/**
 * Extension method to get appropriate context based on Activity or Fragment
 */
fun BaseView.context(isActivity: Boolean): Context {
    return if (isActivity)
        this as BaseActivity
    else
        (this as BaseFragment).activity as BaseActivity
}


fun debugLog(msg: String){
    if(BuildConfig.DEBUG)
        Log.i("DebugLog = ",msg)
}

/**
 * Extension method to get Request Body object from JSONObject.
 */
fun JSONObject.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
}

/**
 * Extension method to get Request Body object from JSONObject.
 */
fun JSONArray.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
}

/**
 * Extension method to get FORM Multipart Body object from String.
 */
fun String.toMultipartBody(): RequestBody {
    return this.toRequestBody(MultipartBody.FORM)
}


/**
 * Extension method to get Image Multipart Body object from File.
 */
fun File.toImagePartBody(): RequestBody {
    return this.asRequestBody("image/*".toMediaTypeOrNull())
}

/**
 * Extension for convert emoji text to server format.
 */

fun EditText.emojiText(): CharSequence {
    return StringEscapeUtils.escapeJava(this.text.toString())
}

