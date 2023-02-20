package net.gearmaniacs.core.model

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude

@Immutable
@Entity(tableName = "tournament")
data class Tournament(
    @PrimaryKey @ColumnInfo(name = "key")
    @get:Exclude override val key: String,
    @ColumnInfo(name = "name")
    val name: String
) : DatabaseClass<Tournament> {

    override fun compareTo(other: Tournament): Int = name.compareTo(other.name)

    override fun copyWithKey(newKey: String): Tournament {
        return this.copy(key = newKey)
    }
}
