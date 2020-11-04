package net.gearmaniacs.core.model.team

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.model.enums.WobbleDeliveryZone

@Parcelize
data class EndGamePeriod(
    @ColumnInfo(name = "power_shot")
    val powerShot: Int,
    @ColumnInfo(name = "wobble_rings")
    val wobbleRings: Int,
    @ColumnInfo(name = "wobble_delivery_zone")
    val wobbleDeliveryZone: Int,
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
        
        score += powerShot * 15
        
        score += wobbleRings * 5
        
        score += when (wobbleDeliveryZone) {
            WobbleDeliveryZone.START_LINE -> 5
            WobbleDeliveryZone.DEAD_ZONE -> 20
            else -> 0
        }

        return score
    }

    companion object {
        private val EMPTY = EndGamePeriod()
    }
}