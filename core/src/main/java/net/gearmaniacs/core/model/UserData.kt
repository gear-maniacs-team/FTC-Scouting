package net.gearmaniacs.core.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserData(
    val id: Int,
    val teamName: String
) : Parcelable {

    constructor() : this(-1, "")
}

val UserData.isEmpty: Boolean
    get() = id == -1 && teamName.isEmpty()

val UserData?.isNullOrEmpty
    get() = this == null || isEmpty
