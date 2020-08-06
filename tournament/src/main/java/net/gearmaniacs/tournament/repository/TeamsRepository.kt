package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventFlow
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.theluckycoder.database.dao.TeamsDao
import javax.inject.Inject

class TeamsRepository @Inject constructor(
    @TournamentActivity.TournamentKey
    private val tournamentKey: String,
    private val teamsDao: TeamsDao,
    private val tournamentReference: DatabaseReference?
) {

    private var teamListForQuery = emptyList<Team>()
    private var lastTeamQuery = ""

    private var listenerScope: CoroutineScope? = null
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

    fun performTeamsSearch(query: String) {
        lastTeamQuery = query
        // Cancel the last running search
        teamSearchJob?.cancel()

        if (teamListForQuery.isEmpty() || query.isEmpty()) {
            queriedTeamsData.value = teamListForQuery
            return
        }

        teamSearchJob = GlobalScope.launch(Dispatchers.Main.immediate) {
            val filteredList = withContext(Dispatchers.Default) {
                val pattern = "(?i).*($query).*".toPattern()

                teamListForQuery.filter {
                    pattern.matcher(it.id.toString() + ' ' + it.name.orEmpty()).matches()
                }
            }

            // Don't update the data if the search was canceled
            ensureActive()
            queriedTeamsData.value = filteredList
        }
    }

    suspend fun addListener() = coroutineScope {
        removeListener()
        listenerScope = this

        launch {
            teamsFlows.collectLatest {
                coroutineContext.ensureActive()

                withContext(Dispatchers.Main) {
                    teamListForQuery = it
                    performTeamsSearch(lastTeamQuery)
                }
            }
        }

        if (!Firebase.isLoggedIn) return@coroutineScope

        launch {
            val databaseReference = tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)

            databaseReference.listValueEventFlow(Team::class).safeCollect {
                teamsDao.replaceTournamentTeams(tournamentKey, it)
            }
        }
    }

    fun removeListener() {
        justTry { listenerScope?.cancel() }
        listenerScope = null
    }
}
