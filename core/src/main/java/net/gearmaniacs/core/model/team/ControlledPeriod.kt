package net.gearmaniacs.core.model.team

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ControlledPeriod(
    @ColumnInfo(name = "low_goal")
    val lowGoal: Int,
    @ColumnInfo(name = "mid_goal")
    val midGoal: Int,
    @ColumnInfo(name = "high_goal")
    val highGoal: Int,
) : Parcelable {

    // Needed for Firebase
    constructor() : this(0, 0, 0)

    @Exclude
    fun isEmpty(): Boolean = this == EMPTY

    @Exclude
    fun isNotEmpty(): Boolean = !isEmpty()

    @Exclude
    fun score(): Int {
        var score = 0

        score += lowGoal * 2
        score += midGoal * 4
        score += highGoal * 6

        return score
    }

    companion object {
        private val EMPTY = ControlledPeriod()
    }
}