package net.gearmaniacs.core.model.enums

import net.gearmaniacs.core.R

object ColorMarker {
    const val DEFAULT = 0
    const val RED = 1
    const val BLUE = 2
    const val GREEN = 3
    const val YELLOW = 4

    fun getResColor(colorMarker: Int): Int = when (colorMarker) {
        DEFAULT -> android.R.color.white
        RED -> R.color.marker_red
        BLUE -> R.color.marker_blue
        GREEN -> R.color.marker_green
        YELLOW -> R.color.marker_yellow
        else -> throw IllegalArgumentException("Invalid ColorMarker: $colorMarker")
    }
}
