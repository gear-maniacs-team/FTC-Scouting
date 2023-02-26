package net.gearmaniacs.core.model.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class StartZone : Parcelable {
    NONE, LEFT, RIGHT;

    override fun toString(): String = when (this) {
        NONE -> "None"
        LEFT -> "Left"
        RIGHT -> "Right"
    }

    companion object {
        fun fromString(zone: String): StartZone = when (zone.lowercase()) {
            "none" -> NONE
            "left" -> LEFT
            "right" -> RIGHT
            else -> throw IllegalArgumentException("Invalid StartZone: $zone")
        }
    }
}