package net.gearmaniacs.core.extensions

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
    isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.toIntOrElse(or: Int = 0): Int = try {
    toInt()
} catch (e: NumberFormatException) {
    or
}

val EditText.textString: String
    get() = text?.toString().orEmpty()
