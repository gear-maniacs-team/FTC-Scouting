package net.gearmaniacs.core.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutonomousData(
    @ColumnInfo(name = "foundation")
    val repositionFoundation: Boolean,
    @ColumnInfo(name = "navigated")
    val navigated: Boolean,
    @ColumnInfo(name = "skystones_delivered")
    val deliveredSkystones: Int,
    @ColumnInfo(name = "stones_delivered")
    val deliveredStones: Int,
    @ColumnInfo(name = "stones_placed")
    val placedStones: Int
) : Parcelable {

    @Suppress("unused") // Needed for Firebase
    constructor() : this(false, false, 0, 0, 0)

    val isEmpty: Boolean
        @Exclude
        get() = !repositionFoundation && deliveredSkystones == 0 && deliveredStones == 0
                && placedStones == 0 && !navigated

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int {
        var score = 0

        if (repositionFoundation) score += 10

        score += if (deliveredSkystones > 2)
            (deliveredSkystones - 2) * 2 + 20
        else
            deliveredSkystones * 10

        score += deliveredStones * 2
        score += placedStones * 4
        if (navigated) score += 5

        return score
    }
}

@Parcelize
data class TeleOpData(
    @ColumnInfo(name = "delivered")
    val deliveredStones: Int,
    @ColumnInfo(name = "placed")
    val placedStones: Int,
    @ColumnInfo(name = "skyscraper_height")
    val skyscraperHeight: Int
) : Parcelable {

    @Suppress("unused") // Needed for Firebase
    constructor() : this(0, 0, 0)

    val isEmpty: Boolean
        @Exclude
        get() = deliveredStones == 0 && placedStones == 0 && skyscraperHeight == 0

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int = deliveredStones + placedStones + skyscraperHeight * 2
}

@Parcelize
data class EndGameData(
    @ColumnInfo(name = "foundation_moved")
    val moveFoundation: Boolean,
    @ColumnInfo(name = "parked")
    val parked: Boolean,
    @ColumnInfo(name = "cap_level")
    val capLevel: Int
) : Parcelable {

    @Suppress("unused") // Needed for Firebase
    constructor() : this(false, false, -1)

    val isEmpty: Boolean
        @Exclude
        get() = !moveFoundation && !parked && capLevel < 0

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int {
        var score = 0

        if (moveFoundation) score += 15
        if (parked) score += 5
        if (capLevel >= 0)
            score += 5 + capLevel // Add 1 point per-level

        return score
    }
}

@Parcelize
@Entity(
    tableName = "skystone_teams",
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
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String? = null,
    @Embedded(prefix = "auto_") val autonomousData: AutonomousData? = null,
    @Embedded(prefix = "teleop_") val teleOpData: TeleOpData? = null,
    @Embedded(prefix = "end_") val endGameData: EndGameData? = null,
    @ColumnInfo(name = "color_marker")
    val colorMarker: Int = ColorMarker.DEFAULT,
    @ColumnInfo(name = "preferred_zone")
    val preferredZone: Int = PreferredZone.NONE,
    @ColumnInfo(name = "notes")
    val notes: String? = null
) : DatabaseClass<Team>(), Parcelable {

    constructor() : this("", "", 0)

    override fun compareTo(other: Team): Int = id.compareTo(other.id)

    val autonomousScore: Int
        @Exclude get() = autonomousData?.calculateScore() ?: 0

    val teleOpScore
        @Exclude get() = teleOpData?.calculateScore() ?: 0

    val endGameScore: Int
        @Exclude get() = endGameData?.calculateScore() ?: 0

    val score: Int
        @Exclude get() = autonomousScore + teleOpScore + endGameScore
}
