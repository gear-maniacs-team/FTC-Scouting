package net.theluckycoder.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Match

@Dao
abstract class MatchesDao {

    @Query("SELECT * FROM skystone_match WHERE tournamentKey = :tournamentKey")
    abstract fun getAllByTournament(tournamentKey: String): Flow<List<Match>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(match: Match)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(list: List<Match>)

    @Query("DELETE from skystone_match WHERE `key` = :matchKey")
    abstract suspend fun delete(matchKey: String)

    @Query("DELETE from skystone_match WHERE tournamentKey = :tournamentKey")
    abstract suspend fun deleteAllFromTournament(tournamentKey: String)

    @Transaction
    open suspend fun replaceTournamentMatches(tournamentKey: String, list: List<Match>) {
        deleteAllFromTournament(tournamentKey)
        insert(list.map { it.copy(tournamentKey = tournamentKey) })
    }
}
