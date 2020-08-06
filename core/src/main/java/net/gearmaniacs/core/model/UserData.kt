package net.gearmaniacs.core.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Parcelize
data class UserData(
    val id: Int,
    val teamName: String
) : Parcelable {

    constructor() : this(-1, "")
}

val UserData.isEmpty: Boolean
    get() = id == -1 && teamName.isEmpty()

@OptIn(ExperimentalContracts::class)
fun UserData?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }

    return this == null || isEmpty
}
