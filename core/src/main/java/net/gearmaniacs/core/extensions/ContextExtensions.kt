package net.gearmaniacs.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.core.content.res.use


inline fun <reified T : Activity> Context.startActivity() =
    startActivity(Intent(this, T::class.java))

fun Context.toast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

inline fun Context.alertDialog(func: AlertDialog.Builder.() -> Unit): AlertDialog.Builder {
    val builder = AlertDialog.Builder(this)
    builder.func()
    return builder
}

fun Activity.hideKeyboard() {
    currentFocus?.let {
        val imm = getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

/**
 * Retrieve a color from the current [android.content.res.Resources.Theme].
 */
@ColorInt
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>() ?: return false
    val nw = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

    return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
}
