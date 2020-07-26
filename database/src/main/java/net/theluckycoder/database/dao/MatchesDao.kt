package net.theluckycoder.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Match

@Dao
abstract class MatchesDao {

    @Query("SELECT * FROM skystone_match WHERE tournamentKey = :tournamentKey")
    abstract fun getAllByTournament(tournamentKey: String): Flow<List<Match>>

    @Insert
    abstract suspend fun insertAll(list: List<Match>)

    @Query("DELETE from skystone_match WHERE `key` = :tournamentKey")
    abstract suspend fun delete(tournamentKey: String)

    @Query("DELETE from skystone_match WHERE tournamentKey = :tournamentKey")
    abstract suspend fun deleteAllFromTournament(tournamentKey: String)

    @Transaction
    open suspend fun replaceTournamentTeams(tournamentKey: String, list: List<Match>) {
        deleteAllFromTournament(tournamentKey)
        insertAll(list.map { it.copy(tournamentKey = tournamentKey) })
    }
}
