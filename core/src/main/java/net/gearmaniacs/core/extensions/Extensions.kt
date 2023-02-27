package net.gearmaniacs.core.extensions

import android.util.Patterns

fun String.isValidEmail(): Boolean =
    isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.toIntOrElse(or: Int = 0): Int = try {
    toInt()
} catch (e: NumberFormatException) {
    or
}
