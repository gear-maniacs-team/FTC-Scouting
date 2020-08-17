package net.gearmaniacs.core.extensions

import android.os.Build
import android.text.Html
import android.text.Spanned
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

val EditText.textString: String
    get() = text?.toString().orEmpty()

@Suppress("DEPRECATION")
fun String.fromHtmlCompat(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(
        this,
        Html.FROM_HTML_MODE_LEGACY
    ) else Html.fromHtml(this)
}
