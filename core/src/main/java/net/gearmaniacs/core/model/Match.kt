package net.gearmaniacs.core.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
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
@Entity(
    tableName = "skystone_match",
    foreignKeys = [ForeignKey(
        entity = Tournament::class,
        parentColumns = ["key"],
        childColumns = ["tournamentKey"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(
        value = ["tournamentKey"],
        name = "indexTournamentKey"
    )]
)
data class Match(
    @PrimaryKey @get:Exclude @set:Exclude override var key: String,
    @get:Exclude val tournamentKey: String = "",
    val id: Int,
    @Embedded(prefix = "red_") val redAlliance: Alliance,
    @Embedded(prefix = "blue_") val blueAlliance: Alliance
) : DatabaseClass<Match>(), Parcelable {

    constructor() : this("", "", 0, Alliance(), Alliance())

    override fun compareTo(other: Match): Int = id.compareTo(other.id)

    fun containsTeam(teamId: Int) =
        redAlliance.containsTeam(teamId) || blueAlliance.containsTeam(teamId)
}
