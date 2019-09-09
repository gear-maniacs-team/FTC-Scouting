package net.gearmaniacs.core.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutonomousData(
    val repositionFoundation: Boolean,
    val deliveredSkystones: Int,
    val deliveredStones: Int,
    val placedStones: Int,
    val parked: Boolean
) : Parcelable {

    constructor() : this(false, 0, 0, 0, false)

    val isEmpty: Boolean
        @Exclude
        get() = !repositionFoundation && deliveredSkystones == 0 && deliveredStones == 0
                && placedStones == 0 && !parked

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
        if (parked) score += 5

        return score
    }
}

@Parcelize
data class TeleOpData(
    val deliveredStones: Int,
    val placedStones: Int
) : Parcelable {

    constructor() : this(0, 0)

    val isEmpty: Boolean
        @Exclude
        get() = deliveredStones == 0 && placedStones == 0

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int {
        return deliveredStones + placedStones
    }
}

@Parcelize
data class EndGame(
    val capPlaced: Boolean,
    val moveFoundation: Boolean,
    val parked: Boolean
) : Parcelable {

    constructor() : this(false, false, false)

    val isEmpty: Boolean
        @Exclude
        get() = !capPlaced && !moveFoundation && !parked

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int {
        var score = 0

        if (capPlaced) score += 5
        if (moveFoundation) score += 15
        if (parked) score += 5

        return score
    }
}

object PreferredLocation {
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
    val endGame: EndGame? = null,
    val preferredLocation: Int = PreferredLocation.NONE,
    val comments: String? = null
) : DatabaseClass<Team>(), Parcelable {

    constructor() : this(0)

    override fun compareTo(other: Team): Int = id.compareTo(other.id)

    val autonomousScore: Int
        @Exclude get() = autonomousData?.calculateScore() ?: 0

    val teleOpScore
        @Exclude get() = teleOpData?.calculateScore() ?: 0

    val endGameScore: Int
        @Exclude get() = endGame?.calculateScore() ?: 0

    val score: Int
        @Exclude get() = autonomousScore + teleOpScore + endGameScore
}
