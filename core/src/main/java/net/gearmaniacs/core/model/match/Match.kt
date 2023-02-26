package net.gearmaniacs.core.model.match

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.model.DatabaseClass
import net.gearmaniacs.core.model.Tournament

@Immutable
@Parcelize
@Entity(
    tableName = "matches",
    foreignKeys = [ForeignKey(
        entity = Tournament::class,
        parentColumns = ["key"],
        childColumns = ["tournament_key"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(
        value = ["tournament_key"],
        name = "index_match_tournament_key"
    )]
)
data class Match(
    @PrimaryKey @ColumnInfo(name = "key")
    @get:Exclude
    override val key: String,

    @ColumnInfo(name = "tournament_key")
    @get:Exclude val tournamentKey: String,

    @ColumnInfo(name = "id")
    val id: Int,

    @Embedded(prefix = "red_")
    val redAlliance: Alliance,

    @Embedded(prefix = "blue_")
    val blueAlliance: Alliance
) : DatabaseClass<Match>, Parcelable {

    constructor() : this("", "", 0, Alliance(), Alliance())

    override fun compareTo(other: Match): Int = id.compareTo(other.id)

    fun containsTeam(teamId: Int) =
        redAlliance.containsTeam(teamId) || blueAlliance.containsTeam(teamId)

    override fun copyWithKey(newKey: String): Match {
        return this.copy(key = newKey)
    }
}
