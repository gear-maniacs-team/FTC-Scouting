package net.gearmaniacs.core.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Alliance(
    @ColumnInfo(name = "first_team")
    val firstTeam: Int,
    @ColumnInfo(name = "second_team")
    val secondTeam: Int,
    @ColumnInfo(name = "score")
    val score: Int
) : Parcelable {

    constructor() : this(0, 0, 0)

    fun containsTeam(teamNumber: Int): Boolean =
        teamNumber == firstTeam || teamNumber == secondTeam
}