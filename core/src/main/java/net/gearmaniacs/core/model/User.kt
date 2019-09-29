package net.gearmaniacs.core.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: Int,
    val teamName: String
) : Parcelable {

    constructor() : this(0, "")

    val isEmpty: Boolean
        @Exclude
        get() = id == 0 && teamName.isEmpty()
}
