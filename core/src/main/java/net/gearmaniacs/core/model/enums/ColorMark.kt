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

    fun getResColor(): Int = when (this) {
        DEFAULT -> android.R.color.white
        RED -> R.color.marker_red
        BLUE -> R.color.marker_blue
        GREEN -> R.color.marker_green
        YELLOW -> R.color.marker_yellow
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
