package net.gearmaniacs.tournament.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class TeamSearchQuery(
    val name: String,
    val defaultMarker: Boolean,
    val redMarker: Boolean,
    val blueMarker: Boolean,
    val greenMarker: Boolean,
    val yellowMarker: Boolean
) : Parcelable {

    constructor() : this("", true, true, true, true, true)

    fun isEmpty() =
        name.isEmpty() && defaultMarker && redMarker && blueMarker && greenMarker && yellowMarker
}