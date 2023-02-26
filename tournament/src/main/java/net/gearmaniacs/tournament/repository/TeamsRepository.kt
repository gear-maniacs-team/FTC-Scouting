package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventFlow
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.database.dao.TeamsDao
import javax.inject.Inject

internal class TeamsRepository @Inject constructor(
    private val teamsDao: TeamsDao,
    private val tournamentReference: DatabaseReference?
) {

    fun getTeamsFlow(tournamentKey: String) = teamsDao.getAllByTournament(tournamentKey)

    suspend fun addTeam(tournamentKey: String, team: Team) {
        val teamRef = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
        }

        insertTeam(tournamentKey, team, teamRef)
    }

    suspend fun addTeams(tournamentKey: String, teams: List<Team>) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
        }

        teams.forEach {
            insertTeam(tournamentKey, it, ref)
        }
    }

    private suspend fun insertTeam(tournamentKey: String, team: Team, ref: DatabaseReference?) {
        val key = if (ref != null) {
            val teamRef = ref.push()
            teamRef.setValue(team)
            teamRef.key!!
        } else {
            Firebase.generatePushId()
        }

        teamsDao.insert(team.copy(key = key, tournamentKey = tournamentKey))
    }

    suspend fun updateTeam(tournamentKey: String, team: Team) {
        teamsDao.insert(team.copy(tournamentKey = tournamentKey))

        if (Firebase.isLoggedIn) {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
                .child(team.key)
                .setValue(team)
                .await()
        }
    }

    suspend fun deleteTeam(tournamentKey: String, teamKey: String) {
        teamsDao.delete(teamKey)

        if (Firebase.isLoggedIn) {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
                .child(teamKey)
                .removeValue()
                .await()
        }
    }

    suspend fun startListener(tournamentKey: String) = coroutineScope {
        if (!Firebase.isLoggedIn) return@coroutineScope

        val databaseReference = tournamentReference!!
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)

        databaseReference.listValueEventFlow(Team::class).safeCollect {
            teamsDao.replaceTournamentTeams(tournamentKey, it)
        }
    }
}
