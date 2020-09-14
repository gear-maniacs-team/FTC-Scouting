package net.gearmaniacs.core.model.team

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutonomousPeriod(
    @ColumnInfo(name = "wobble_delivery")
    val wobbleDelivery: Boolean,
    @ColumnInfo(name = "low_goal")
    val lowGoal: Int,
    @ColumnInfo(name = "mid_goal")
    val midGoal: Int,
    @ColumnInfo(name = "high_goal")
    val highGoal: Int,
    @ColumnInfo(name = "power_shot")
    val powerShot: Int,
    @ColumnInfo(name = "parked")
    val parked: Boolean
) : Parcelable {

    // Needed for Firebase
    constructor() : this(false, 0, 0, 0, 0, false)

    @Exclude
    fun isEmpty(): Boolean = this == EMPTY

    @Exclude
    fun isNotEmpty(): Boolean = !isEmpty()

    @Exclude
    fun score(): Int {
        var score = 0

        if (wobbleDelivery)
            score += 15
        
        score += lowGoal * 3
        score += midGoal * 6
        score += highGoal * 12
        score += powerShot * 15

        if (parked)
            score += 5

        return score
    }

    companion object {
        private val EMPTY = AutonomousPeriod()
    }
}