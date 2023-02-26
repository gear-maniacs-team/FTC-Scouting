package net.gearmaniacs.core.model.team

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.model.DatabaseClass
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.enums.ColorMark
import net.gearmaniacs.core.model.enums.StartZone

@Immutable
@Parcelize
@Entity(
    tableName = "teams",
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
    @get:Exclude
    override val key: String,

    @ColumnInfo(name = "tournament_key")
    @get:Exclude
    val tournamentKey: String = "",

    @ColumnInfo(name = "number")
    override val number: Int,

    @ColumnInfo(name = "name")
    override val name: String? = null,

    @ColumnInfo(name = "auto_score")
    val autonomousScore: Int = 0,

    @ColumnInfo(name = "teleop_score")
    val teleOpScore: Int = 0,

    @ColumnInfo(name = "color_mark")
    val colorMark: ColorMark = ColorMark.DEFAULT,

    @ColumnInfo(name = "start_zone")
    val startZone: StartZone = StartZone.NONE,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) : BaseTeam(number, name), DatabaseClass<Team>, Parcelable {

    constructor() : this("", "", 0)

    override fun compareTo(other: Team): Int = number.compareTo(other.number)

    fun totalScore(): Int = autonomousScore + teleOpScore

    override fun copyWithKey(newKey: String): Team {
        return this.copy(key = newKey)
    }
}
