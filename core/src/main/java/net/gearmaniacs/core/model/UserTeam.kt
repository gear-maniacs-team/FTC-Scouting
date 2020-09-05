package net.gearmaniacs.core.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Parcelize
data class UserTeam(
    val id: Int,
    val teamName: String
) : Parcelable {

    constructor() : this(-1, "")
}

val UserTeam.isEmpty: Boolean
    get() = id == -1 && teamName.isEmpty()

@OptIn(ExperimentalContracts::class)
fun UserTeam?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }

    return this == null || isEmpty
}
