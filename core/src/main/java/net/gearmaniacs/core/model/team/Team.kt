package net.gearmaniacs.core.model.team

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize
import net.gearmaniacs.core.model.DatabaseClass
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.enums.ColorMarker
import net.gearmaniacs.core.model.enums.PreferredZone

@Parcelize
@Entity(
    tableName = "ultimate_goal_teams",
    foreignKeys = [ForeignKey(
        entity = Tournament::class,
        parentColumns = ["key"],
        childColumns = ["tournament_key"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(
        value = ["tournament_key"],
        name = "index_team_tournament_key"
    )]
)
data class Team(
    @PrimaryKey @ColumnInfo(name = "key")
    @get:Exclude @set:Exclude override var key: String,
    @ColumnInfo(name = "tournament_key")
    @get:Exclude val tournamentKey: String = "",
    @ColumnInfo(name = "number")
    override val number: Int,
    @ColumnInfo(name = "name")
    override val name: String? = null,
    @Embedded(prefix = "auto_") val autonomousPeriod: AutonomousPeriod? = null,
    @Embedded(prefix = "controlled_") val controlledPeriod: ControlledPeriod? = null,
    @Embedded(prefix = "end_") val endGamePeriod: EndGamePeriod? = null,
    @ColumnInfo(name = "color_marker")
    val colorMarker: Int = ColorMarker.DEFAULT,
    @ColumnInfo(name = "preferred_zone")
    val preferredZone: Int = PreferredZone.NONE,
    @ColumnInfo(name = "notes")
    val notes: String? = null
) : BaseTeam(number, name), DatabaseClass<Team>, Parcelable {

    constructor() : this("", "", 0)

    override fun compareTo(other: Team): Int = number.compareTo(other.number)

    @Exclude
    fun autonomousScore(): Int = autonomousPeriod?.score() ?: 0

    @Exclude
    fun controlledScore() = controlledPeriod?.score() ?: 0

    @Exclude
    fun endGameScore(): Int = endGamePeriod?.score() ?: 0

    @Exclude
    fun score(): Int = autonomousScore() + controlledScore() + endGameScore()
}
