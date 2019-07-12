package net.gearmaniacs.ftcscouting.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: Int,
    val teamName: String
) : Parcelable
