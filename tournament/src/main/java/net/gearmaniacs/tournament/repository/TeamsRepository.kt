package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.*
import net.gearmaniacs.core.model.Team
import net.theluckycoder.database.dao.TeamsDao

class TeamsRepository(
    private val teamsDao: TeamsDao,
    private val tournamentReference: DatabaseReference?
) {

    private var teamListForQuery = emptyList<Team>()
    private var lastTeamQuery = ""
    private var teamSearchJob: Job? = null // Last launched search job
    private var valueEventListenerJob: Job? = null

    fun getTeamsFlow(tournamentKey: String) = teamsDao.getAllByTournament(tournamentKey)

    val queriedLiveData = MutableNonNullLiveData(emptyList<Team>())

    suspend fun addTeam(tournamentKey: String, team: Team) {
        val teamRef = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
        }

        insertTeam(team, tournamentKey, teamRef)
    }

    suspend fun addTeams(tournamentKey: String, teams: List<Team>) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
        }

        teams.forEach {
            insertTeam(it, tournamentKey, ref)
        }
    }

    private suspend fun insertTeam(team: Team, tournamentKey: String, ref: DatabaseReference?) {
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

    fun performTeamsSearch(query: String) {
        lastTeamQuery = query
        // Cancel the last running search
        teamSearchJob?.cancel()

        if (teamListForQuery.isEmpty() || query.isEmpty()) {
            queriedLiveData.value = teamListForQuery
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
            queriedLiveData.value = filteredList
        }
    }

    suspend fun addListener(tournamentKey: String) = coroutineScope {
        justTry { valueEventListenerJob?.cancelAndJoin() }

        if (!Firebase.isLoggedIn) return@coroutineScope

        valueEventListenerJob = launch {
            val databaseReference = tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)

            databaseReference.listValueEventListenerFlow(Team::class).safeCollect {
                teamsDao.replaceTournamentTeams(tournamentKey, it)

                launch(Dispatchers.Main) {
                    teamListForQuery = it
                    performTeamsSearch(lastTeamQuery)
                }
            }
        }
    }

    fun removeListener() {
        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }
}
