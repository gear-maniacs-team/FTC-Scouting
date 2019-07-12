package net.gearmaniacs.ftcscouting.utils.extensions

import android.util.Patterns
import android.widget.EditText

fun <T> lazyFast(operation: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, operation)

inline fun justTry(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun String.isValidEmail(): Boolean =
    isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.toIntOrDefault(default: Int = 0): Int = try {
    toInt()
} catch (e: NumberFormatException) {
    default
}

fun EditText.getTextOrEmpty(): String =
    text?.toString() ?: ""
