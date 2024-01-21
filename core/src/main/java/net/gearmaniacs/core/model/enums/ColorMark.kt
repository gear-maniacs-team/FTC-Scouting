package net.gearmaniacs.core.model.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.R

@Parcelize
enum class ColorMark : Parcelable {
    DEFAULT,
    RED,
    BLUE,
    GREEN,
    YELLOW;

    fun getColorResource(): Int = when (this) {
        DEFAULT -> android.R.color.darker_gray
        RED -> R.color.marker_red
        BLUE -> R.color.marker_blue
        GREEN -> R.color.marker_green
        YELLOW -> R.color.marker_yellow
    }

    fun getLabelResource() = when (this) {
        DEFAULT -> R.string.color_default
        RED -> R.string.color_red
        BLUE -> R.string.color_blue
        GREEN -> R.string.color_green
        YELLOW -> R.string.color_yellow
    }

    override fun toString() = when (this) {
        DEFAULT -> "Default"
        RED -> "Red"
        BLUE -> "Blue"
        GREEN -> "Green"
        YELLOW -> "Yellow"
    }

    companion object {
        fun fromString(colorMarker: String) = when (colorMarker.lowercase()) {
            "default" -> DEFAULT
            "red" -> RED
            "blue" -> BLUE
            "green" -> GREEN
            "yellow" -> YELLOW
            else -> throw IllegalArgumentException("Invalid ColorMark: $colorMarker")
        }
    }
}
