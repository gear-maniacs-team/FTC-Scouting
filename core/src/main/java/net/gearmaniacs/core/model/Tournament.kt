package net.gearmaniacs.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude

@Entity(tableName = "skystone_tournament")
data class Tournament(
    @PrimaryKey @get:Exclude @set:Exclude override var key: String,
    val name: String
) : DatabaseClass<Tournament>() {

    override fun compareTo(other: Tournament): Int = name.compareTo(other.name)
}
