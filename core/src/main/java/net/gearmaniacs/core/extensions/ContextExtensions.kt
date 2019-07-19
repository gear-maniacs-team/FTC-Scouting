package net.gearmaniacs.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

inline fun <reified T : Activity> Context.startActivity() = startActivity(Intent(this, T::class.java))

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

fun Context.dpFromPx(px: Float): Float {
    return px / resources.displayMetrics.density
}

fun Context.pxFromDp(dp: Float): Float {
    return dp * resources.displayMetrics.density
}

fun Activity.checkRuntimePermission(permission: String, requestCode: Int = 200) {
    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
}
