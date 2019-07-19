package net.gearmaniacs.core.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutonomousData(
    val latching: Boolean,
    val sampling: Boolean,
    val marker: Boolean,
    val parking: Boolean,
    val minerals: Int
) : Parcelable {

    constructor() : this(false, false, false, false, 0)

    val isEmpty: Boolean
        @Exclude
        get() = !latching && !sampling && !marker && !parking && minerals == 0

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int {
        var result = 0

        if (latching) result += 30
        if (sampling) result += 25
        if (marker) result += 15
        if (parking) result += 10
        result += minerals * 5

        return result
    }
}

@Parcelize
data class TeleOpData(
    val depotMinerals: Int,
    val landerMinerals: Int
) : Parcelable {

    constructor() : this(0, 0)

    val isEmpty: Boolean
        @Exclude
        get() = depotMinerals == 0 && landerMinerals == 0

    val isNotEmpty: Boolean
        @Exclude
        get() = !isEmpty

    fun calculateScore(): Int {
        var result = 0

        result += depotMinerals * 2
        result += landerMinerals * 5

        return result
    }
}

object PreferredLocation {
    const val NONE = 0
    const val DEPOT = 1
    const val CRATER = 2
}

object EndGame {
    const val NONE = 0
    const val ROBOT_LATCHED = 1
    const val PARTIALLY_PARKED = 2
    const val COMPLETELY_PARKED = 3
}

@Parcelize
data class Team(
    val id: Int,
    val name: String? = null,
    val autonomousData: AutonomousData? = null,
    val teleOpData: TeleOpData? = null,
    val endGame: Int = EndGame.NONE,
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
        @Exclude get() = when (endGame) {
            EndGame.ROBOT_LATCHED -> 50
            EndGame.PARTIALLY_PARKED -> 15
            EndGame.COMPLETELY_PARKED -> 25
            else -> 0
        }

    val score: Int
        @Exclude get() = autonomousScore + teleOpScore + endGameScore
}
