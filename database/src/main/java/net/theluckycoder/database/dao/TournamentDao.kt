package net.theluckycoder.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Tournament

@Dao
interface TournamentDao {

    @Query("SELECT * from skystone_tournament")
    fun getAll(): Flow<List<Tournament>>

    @Insert
    suspend fun insert(tournament: Tournament)

    @Query("DELETE from skystone_tournament WHERE `key` = :tournamentKey")
    suspend fun delete(tournamentKey: String)
}
