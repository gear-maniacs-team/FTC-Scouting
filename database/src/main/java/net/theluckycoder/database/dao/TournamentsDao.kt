package net.theluckycoder.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Tournament

@Dao
abstract class TournamentsDao {

    @Query("SELECT * from skystone_tournament")
    abstract suspend fun getAll(): List<Tournament>

    @Query("SELECT * from skystone_tournament")
    abstract fun getAllFlow(): Flow<List<Tournament>>

    @Query("SELECT * from skystone_tournament WHERE `key` = :tournamentKey")
    abstract fun getFlow(tournamentKey: String): Flow<Tournament?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tournament: Tournament)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(list: List<Tournament>)

    @Query("DELETE from skystone_tournament WHERE `key` = :tournamentKey")
    abstract suspend fun delete(tournamentKey: String)

    @Delete
    abstract suspend fun delete(list: List<Tournament>)

    @Query("DELETE from skystone_tournament")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(list: List<Tournament>) {
        val currentData = getAll()
        delete((currentData subtract list).toList())
        insert((list subtract currentData).toList())
    }
}
