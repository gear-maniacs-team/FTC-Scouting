package net.theluckycoder.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Team

@Dao
abstract class TeamsDao {

    @Query("SELECT * FROM skystone_teams WHERE tournament_key = :tournamentKey ORDER BY id ASC")
    abstract fun getAllByTournament(tournamentKey: String): Flow<List<Team>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(team: Team)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(list: List<Team>)

    @Query("DELETE from skystone_teams WHERE `key` = :teamKey")
    abstract suspend fun delete(teamKey: String)

    @Query("DELETE from skystone_teams WHERE tournament_key = :tournamentKey")
    abstract suspend fun deleteAllFromTournament(tournamentKey: String)

    @Transaction
    open suspend fun replaceTournamentTeams(tournamentKey: String, list: List<Team>) {
        deleteAllFromTournament(tournamentKey)
        insert(list.map { it.copy(tournamentKey = tournamentKey) })
    }
}
