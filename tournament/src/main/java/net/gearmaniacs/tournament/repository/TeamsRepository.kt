package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.listValueEventListenerFlow
import net.gearmaniacs.core.model.Team
import net.theluckycoder.database.dao.TeamsDao

class TeamsRepository(
    private val teamsDao: TeamsDao,
    private val tournamentReference: DatabaseReference
) {

    private var lastTeamQuery = ""
    private var teamSearchJob: Job? = null // Last launched search job
    private var valueEventListenerJob: Job? = null

    fun getTeamsFlow(tournamentKey: String) = teamsDao.getAllByTournament(tournamentKey)

    val queriedLiveData = MutableNonNullLiveData(emptyList<Team>())

    fun addTeam(tournamentKey: String, team: Team) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .push()
            .setValue(team)
    }

    fun addTeams(tournamentKey: String, teams: List<Team>) {
        val ref = tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)

        teams.forEach {
            ref.push().setValue(it)
        }
    }

    fun updateTeam(tournamentKey: String, team: Team) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .child(team.key)
            .setValue(team)
    }

    fun deleteTeam(tournamentKey: String, teamKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .child(teamKey)
            .removeValue()
    }

    fun performTeamsSearch(teamList: List<Team>, query: String) {
        lastTeamQuery = query
        // Cancel the last running search
        teamSearchJob?.cancel()

        if (teamList.isEmpty() || query.isEmpty()) {
            queriedLiveData.value = teamList
            return
        }

        teamSearchJob = GlobalScope.launch(Dispatchers.Main.immediate) {
            val filteredList = withContext(Dispatchers.Default) {
                val pattern = "(?i).*($query).*".toPattern()

                teamList.filter {
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

        valueEventListenerJob = launch {
            val databaseReference = tournamentReference
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)

            databaseReference.listValueEventListenerFlow(Team::class).safeCollect {
                teamsDao.replaceTournamentTeams(tournamentKey, it)
                println(it.size)
                /*withContext(Dispatchers.Main) {
                    performTeamsSearch(it, lastTeamQuery)
                }*/
            }
        }
    }

    fun removeListener() {
        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }
}
