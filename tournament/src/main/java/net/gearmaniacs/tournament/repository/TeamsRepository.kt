package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventFlow
import net.gearmaniacs.core.model.enums.ColorMarker
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.ui.model.TeamSearchQuery
import net.gearmaniacs.tournament.utils.filterTeamsByQuery
import net.gearmaniacs.database.dao.TeamsDao
import javax.inject.Inject

internal class TeamsRepository @Inject constructor(
    private val teamsDao: TeamsDao,
    private val tournamentReference: DatabaseReference?
) {

    private var teamQueryJob: Job? = null // Last launched query job
    private val teamQueryFlow = MutableStateFlow(TeamSearchQuery())
    private val _queriedTeamsFlow = MutableStateFlow(emptyList<Team>())
    val queriedTeamsFlow: StateFlow<List<Team>> = _queriedTeamsFlow

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

    fun updateTeamsQuery(query: TeamSearchQuery) {
        teamQueryFlow.value = query
    }

    private suspend fun performTeamsQuery(
        list: List<Team>,
        query: TeamSearchQuery
    ): Unit = coroutineScope {
        // Cancel the last running search
        teamQueryJob?.cancel()

        if (list.isEmpty() || query.isEmpty()) {
            _queriedTeamsFlow.value = list
            return@coroutineScope
        }

        teamQueryJob = launch(Dispatchers.Default) {
            var filteredList = list.filter {
                val marker = it.colorMarker
                (query.defaultMarker && marker == ColorMarker.DEFAULT)
                        || (query.redMarker && marker == ColorMarker.RED)
                        || (query.blueMarker && marker == ColorMarker.BLUE)
                        || (query.greenMarker && marker == ColorMarker.GREEN)
                        || (query.yellowMarker && marker == ColorMarker.YELLOW)
            }

            ensureActive()

            if (query.name.isNotEmpty()) {
                filteredList = filteredList.asSequence().filterTeamsByQuery(query.name).toList()
            }

            // Don't update the data if the search was canceled
            ensureActive()
            _queriedTeamsFlow.value = filteredList.toList()
        }
    }

    suspend fun startListener(tournamentKey: String) = coroutineScope {
        launch {
            getTeamsFlow(tournamentKey).combine(teamQueryFlow) { b1, b2 ->
                b1 to b2
            }.collectLatest {
                coroutineContext.ensureActive()

                performTeamsQuery(it.first, it.second)
            }
        }

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
