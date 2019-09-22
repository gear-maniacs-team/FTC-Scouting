package net.gearmaniacs.core.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutonomousData(
    val repositionFoundation: Boolean,
    val navigated: Boolean,
    val deliveredSkystones: Int,
    val deliveredStones: Int,
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
    val deliveredStones: Int,
    val placedStones: Int
) : Parcelable {

    @Suppress("unused") // Needed for Firebase
    constructor() : this(0, 0)

    val isEmpty: Boolean
        @Exclude
        get() = deliveredStones == 0 && placedStones == 0

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int = deliveredStones + placedStones
}

@Parcelize
data class EndGameData(
    val moveFoundation: Boolean,
    val parked: Boolean,
    val capLevel: Int
) : Parcelable {

    @Suppress("unused") // Needed for Firebase
    constructor() : this(false, false, 0)

    val isEmpty: Boolean
        @Exclude
        get() = !moveFoundation && !parked && capLevel == 0

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int {
        var score = 0

        if (moveFoundation) score += 15
        if (parked) score += 5
        if (capLevel > 0)
            score += 5 + capLevel // Add 1 point per-level

        return score
    }
}

object PreferredZone {
    const val NONE = 0
    const val BUILDING = 1
    const val LOADING = 2
}

@Parcelize
data class Team(
    val id: Int,
    val name: String? = null,
    val autonomousData: AutonomousData? = null,
    val teleOpData: TeleOpData? = null,
    val endGameData: EndGameData? = null,
    val preferredZone: Int = PreferredZone.NONE,
    val notes: String? = null
) : DatabaseClass<Team>(), Parcelable {

    constructor() : this(0)

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
