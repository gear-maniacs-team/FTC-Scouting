package net.theluckycoder.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Tournament

@Dao
abstract class TournamentsDao {

    @Query("SELECT * from skystone_tournament")
    abstract fun getAll(): Flow<List<Tournament>>

    @Insert
    abstract suspend fun insert(tournament: Tournament)

    @Insert
    abstract suspend fun insertAll(list: List<Tournament>)

    @Query("DELETE from skystone_tournament WHERE `key` = :tournamentKey")
    abstract suspend fun delete(tournamentKey: String)

    @Query("DELETE from skystone_tournament")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(list: List<Tournament>) {
        deleteAll()
        insertAll(list)
    }
}
