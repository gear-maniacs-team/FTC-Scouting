package net.gearmaniacs.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude

@Entity(tableName = "skystone_tournament")
data class Tournament(
    @PrimaryKey @ColumnInfo(name = "key")
    @get:Exclude @set:Exclude override var key: String,
    @ColumnInfo(name = "name")
    val name: String
) : DatabaseClass<Tournament>() {

    override fun compareTo(other: Tournament): Int = name.compareTo(other.name)
}
