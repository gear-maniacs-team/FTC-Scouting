package net.gearmaniacs.tournament.repository

import androidx.lifecycle.Observer
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseChildListener
import net.gearmaniacs.core.firebase.FirebaseSingleValueListener
import net.gearmaniacs.core.model.Team

class TeamsRepository(private val tournamentReference: DatabaseReference) {

    val liveData = MutexLiveData(emptyList<Team>())
    val queriedLiveData = NonNullLiveData(emptyList<Team>())

    private val queryObserver = Observer<List<Team>> {
        performTeamsSearch(lastTeamQuery)
    }
    private val teamsListener = FirebaseChildListener(Team::class.java, liveData)
    private var listenersInitialized = false

    private var lastTeamQuery = ""
    private var teamSearchJob: Job? = null // Last launched search job

    fun addTeam(tournamentKey: String, team: Team) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .push()
            .setValue(team)
    }

    fun addTeams(tournamentKey: String, teamIds: List<Team>) {
        val ref = tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)

        teamIds.forEach {
            ref.push().setValue(it)
        }
    }

    fun updateTeam(tournamentKey: String, team: Team) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .child(team.key!!)
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

    fun performTeamsSearch(query: String) {
        lastTeamQuery = query
        // Cancel the last running search
        teamSearchJob?.cancel()

        val teamList = liveData.value

        if (teamList.isEmpty() || query.isEmpty()) {
            queriedLiveData.value = teamList
            return
        }

        teamSearchJob = GlobalScope.launch(Dispatchers.Main.immediate) {
            val filteredList = withContext(Dispatchers.Default) {
                val pattern = "(?i).*($query).*".toPattern()

                teamList.filter {
                    pattern.matcher(it.name.orEmpty()).matches()
                            || pattern.matcher(it.id.toString()).matches()
                }
            }

            // Don't update the data if the search was canceled
            ensureActive()
            queriedLiveData.value = filteredList
        }
    }

    fun addListener(tournamentKey: String) {
        val tournamentsListRef = tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)

        val teamsRef = tournamentsListRef.child(DatabasePaths.KEY_TEAMS)

        if (!listenersInitialized) {
            teamsRef.addListenerForSingleValueEvent(
                FirebaseSingleValueListener(Team::class.java, liveData)
            )
            listenersInitialized = true
        }

        teamsRef.addChildEventListener(teamsListener)

        liveData.observeForever(queryObserver)
    }

    fun removeListener(tournamentKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .removeEventListener(teamsListener)

        liveData.removeObserver(queryObserver)
    }
}