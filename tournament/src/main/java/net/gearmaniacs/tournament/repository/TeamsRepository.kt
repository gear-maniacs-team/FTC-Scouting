package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventFlow
import net.gearmaniacs.core.model.ColorMarker
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.utils.AbstractListenerRepository
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.adapter.TeamSearchAdapter
import net.theluckycoder.database.dao.TeamsDao
import javax.inject.Inject

internal class TeamsRepository @Inject constructor(
    @TournamentActivity.TournamentKey
    private val tournamentKey: String,
    private val teamsDao: TeamsDao,
    private val tournamentReference: DatabaseReference?
) : AbstractListenerRepository() {

    private var teamListForQuery = emptyList<Team>()
    private var lastTeamQuery: TeamSearchAdapter.Query? = null

    private var teamSearchJob: Job? = null // Last launched search job

    val teamsFlows = teamsDao.getAllByTournament(tournamentKey)
    val queriedTeamsData = MutableNonNullLiveData(emptyList<Team>())

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

    suspend fun performTeamsSearch(query: TeamSearchAdapter.Query?): Unit = coroutineScope {
        lastTeamQuery = query
        val list = teamListForQuery
        // Cancel the last running search
        teamSearchJob?.cancel()

        if (list.isEmpty() || query == null || query.isEmpty()) {
            queriedTeamsData.value = list
            return@coroutineScope
        }

        teamSearchJob = launch(Dispatchers.Main.immediate) {
            val filteredList = withContext(Dispatchers.Default) {
                var newList = list.filter {
                    (query.defaultMarker && it.colorMarker == ColorMarker.DEFAULT)
                            || (query.redMarker && it.colorMarker == ColorMarker.RED)
                            || (query.blueMarker && it.colorMarker == ColorMarker.BLUE)
                            || (query.greenMarker && it.colorMarker == ColorMarker.GREEN)
                            || (query.purpleMarker && it.colorMarker == ColorMarker.PURPLE)
                            || (query.yellowMarker && it.colorMarker == ColorMarker.YELLOW)
                }

                if (query.name.isNotEmpty()) {
                    val pattern = "(?i).*(${query.name}).*".toPattern()

                    newList = newList.filter {
                        pattern.matcher(it.id.toString() + ' ' + it.name.orEmpty()).matches()
                    }
                }

                newList.toList()
            }

            // Don't update the data if the search was canceled
            ensureActive()
            queriedTeamsData.value = filteredList
        }
    }

    override suspend fun onListenerAdded(scope: CoroutineScope) {
        scope.launch {
            teamsFlows.collectLatest {
                coroutineContext.ensureActive()

                withContext(Dispatchers.Main) {
                    teamListForQuery = it
                    performTeamsSearch(lastTeamQuery)
                }
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
