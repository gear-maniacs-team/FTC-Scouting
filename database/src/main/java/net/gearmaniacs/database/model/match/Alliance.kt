package net.gearmaniacs.database.model.match

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
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