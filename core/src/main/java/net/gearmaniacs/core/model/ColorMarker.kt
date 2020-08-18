package net.gearmaniacs.core.model

import android.content.Context
import android.graphics.Color
import net.gearmaniacs.core.R
import net.gearmaniacs.core.extensions.getColorCompat

object ColorMarker {
    const val DEFAULT = 0
    const val RED = 1
    const val BLUE = 2
    const val GREEN = 3
    const val PURPLE = 4
    const val YELLOW = 5

    fun getHexColor(colorMarker: Int, context: Context): Int = when (colorMarker) {
        DEFAULT -> Color.WHITE
        RED -> context.getColorCompat(R.color.marker_red)
        BLUE -> context.getColorCompat(R.color.marker_blue)
        GREEN -> context.getColorCompat(R.color.marker_green)
        PURPLE -> context.getColorCompat(R.color.marker_purple)
        YELLOW -> context.getColorCompat(R.color.marker_yellow)
        else -> throw IllegalArgumentException("Invalid ColorMarker: $colorMarker")
    }
}
