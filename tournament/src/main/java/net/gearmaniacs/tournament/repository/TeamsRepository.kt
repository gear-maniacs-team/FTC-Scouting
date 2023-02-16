package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
import net.gearmaniacs.core.utils.AbstractListenerRepository
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.adapter.TeamSearchAdapter
import net.theluckycoder.database.dao.TeamsDao
import javax.inject.Inject

internal class TeamsRepository @Inject constructor(
    tournamentKey: TournamentActivity.TournamentKey,
    private val teamsDao: TeamsDao,
    private val tournamentReference: DatabaseReference?
) : AbstractListenerRepository() {

    private val tournamentKey = tournamentKey.value
    private var teamQueryJob: Job? = null // Last launched query job
    private val teamQueryStateFlow = MutableStateFlow<TeamSearchAdapter.Query?>(null)
    private val _queriedTeamsFlow = MutableStateFlow(emptyList<Team>())
    val queriedTeamsFlow: StateFlow<List<Team>> = _queriedTeamsFlow

    val teamsFlows = teamsDao.getAllByTournament(this.tournamentKey).distinctUntilChanged()


    suspend fun addTeam(team: Team) {
        val teamRef = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
        }

        insertTeam(team, teamRef)
    }

    suspend fun addTeams(teams: List<Team>) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
        }

        teams.forEach {
            insertTeam(it, ref)
        }
    }

    private suspend fun insertTeam(team: Team, ref: DatabaseReference?) {
        val key = if (ref != null) {
            val teamRef = ref.push()
            teamRef.setValue(team)
            teamRef.key!!
        } else {
            Firebase.generatePushId()
        }

        teamsDao.insert(team.copy(key = key, tournamentKey = tournamentKey))
    }

    suspend fun updateTeam(team: Team) {
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

    suspend fun deleteTeam(teamKey: String) {
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

    fun updateTeamsQuery(query: TeamSearchAdapter.Query?) {
        teamQueryStateFlow.value = query
    }

    private suspend fun performTeamsQuery(
        list: List<Team>,
        query: TeamSearchAdapter.Query?
    ): Unit = coroutineScope {
        // Cancel the last running search
        teamQueryJob?.cancel()

        if (list.isEmpty() || query == null || query.isEmpty()) {
            _queriedTeamsFlow.value = list
            return@coroutineScope
        }

        teamQueryJob = launch(Dispatchers.Default) {
            var filteredList = list.filter {
                (query.defaultMarker && it.colorMarker == ColorMarker.DEFAULT)
                        || (query.redMarker && it.colorMarker == ColorMarker.RED)
                        || (query.blueMarker && it.colorMarker == ColorMarker.BLUE)
                        || (query.greenMarker && it.colorMarker == ColorMarker.GREEN)
                        || (query.yellowMarker && it.colorMarker == ColorMarker.YELLOW)
            }

            if (query.name.isNotEmpty()) {
                val pattern = "(?i).*(${query.name}).*".toPattern()

                filteredList = filteredList.filter {
                    pattern.matcher(it.number.toString() + ' ' + it.name.orEmpty()).matches()
                }
            }

            // Don't update the data if the search was canceled
            ensureActive()
            _queriedTeamsFlow.value = filteredList.toList()
        }
    }

    override suspend fun onListenerAdded(scope: CoroutineScope) {
        scope.launch {
            teamsFlows.combine(teamQueryStateFlow) { b1, b2 ->
                b1 to b2
            }.collectLatest {
                coroutineContext.ensureActive()

                performTeamsQuery(it.first, it.second)
            }
        }

        if (!Firebase.isLoggedIn) return

        scope.launch {
            val databaseReference = tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)

            databaseReference.listValueEventFlow(Team::class).safeCollect {
                teamsDao.replaceTournamentTeams(tournamentKey, it)
            }
        }
    }
}
