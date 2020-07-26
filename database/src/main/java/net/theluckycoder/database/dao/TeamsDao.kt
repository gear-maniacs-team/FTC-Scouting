package net.theluckycoder.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Team

@Dao
abstract class TeamsDao {

    @Query("SELECT * FROM skystone_team WHERE tournamentKey = :tournamentKey")
    abstract fun getAllByTournament(tournamentKey: String): Flow<List<Team>>

    @Insert
    abstract suspend fun insertAll(list: List<Team>)

    @Query("DELETE from skystone_team WHERE `key` = :tournamentKey")
    abstract suspend fun delete(tournamentKey: String)

    @Query("DELETE from skystone_team WHERE tournamentKey = :tournamentKey")
    abstract suspend fun deleteAllFromTournament(tournamentKey: String)

    @Transaction
    open suspend fun replaceTournamentTeams(tournamentKey: String, list: List<Team>) {
        deleteAllFromTournament(tournamentKey)
        insertAll(list.map { it.copy(tournamentKey = tournamentKey) })
    }
}
