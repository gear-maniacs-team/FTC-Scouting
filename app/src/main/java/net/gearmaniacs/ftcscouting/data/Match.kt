package net.gearmaniacs.ftcscouting.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Alliance(
    val firstTeam: Int,
    val secondTeam: Int,
    val score: Int
) : Parcelable {

    constructor() : this(0, 0, 0)

    fun containsTeam(teamNumber: Int): Boolean =
        teamNumber == firstTeam || teamNumber == secondTeam
}

@Parcelize
data class Match(
    val id: Int,
    val redAlliance: Alliance,
    val blueAlliance: Alliance
) : DatabaseClass<Match>(), Parcelable {

    constructor() : this(0, Alliance(), Alliance())

    override fun compareTo(other: Match): Int = id.compareTo(other.id)

    fun containsTeam(teamId: Int) = redAlliance.containsTeam(teamId) || blueAlliance.containsTeam(teamId)
}
