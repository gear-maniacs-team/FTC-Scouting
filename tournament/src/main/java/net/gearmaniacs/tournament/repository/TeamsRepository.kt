package net.gearmaniacs.tournament.repository

import androidx.lifecycle.Observer
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseChildListener
import net.gearmaniacs.core.firebase.FirebaseSingleValueListener
import net.gearmaniacs.core.model.Team

class TeamsRepository(private val currentUserRef: DatabaseReference) {

    val liveData = MutexLiveData(emptyList<Team>())
    val queriedLiveData = NonNullLiveData(emptyList<Team>())

    private val queryOberver = Observer<List<Team>> {
        runBlocking {
            performTeamsSearch(lastTeamQuery)
        }
    }
    private val teamsListener = FirebaseChildListener(Team::class.java, liveData)
    private var listenersInitialized = false

    private var lastTeamQuery = ""
    private var teamSearchJob: Job? = null // Last launched search job

    fun addTeam(tournamentKey: String, team: Team) {
        currentUserRef
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .push()
            .setValue(team)
    }

    fun addTeams(tournamentKey: String, teamIds: List<Team>) {
        val ref = currentUserRef
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)

        teamIds.forEach {
            ref.push().setValue(it)
        }
    }

    fun updateTeam(tournamentKey: String, team: Team) {
        currentUserRef
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .child(team.key!!)
            .setValue(team)
    }

    fun deleteTeam(tournamentKey: String, teamKey: String) {
        currentUserRef
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .child(teamKey)
            .removeValue()
    }

    suspend fun performTeamsSearch(query: String) = coroutineScope {
        lastTeamQuery = query
        // Cancel the last running search
        teamSearchJob?.cancel()

        val teamList = liveData.value

        if (teamList.isEmpty() || query.isEmpty()) {
            queriedLiveData.value = teamList
            return@coroutineScope
        }

        teamSearchJob = launch(Dispatchers.Main.immediate) {
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

    fun addListeners(tournamentKey: String) {
        val tournamentRef = currentUserRef
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)

        val teamsRef = tournamentRef.child(DatabasePaths.KEY_TEAMS)

        if (!listenersInitialized) {
            teamsRef.addListenerForSingleValueEvent(
                FirebaseSingleValueListener(Team::class.java, liveData)
            )
            listenersInitialized = true
        }

        teamsRef.addChildEventListener(teamsListener)

        queriedLiveData.observeForever(queryOberver)
    }

    fun removeListeners(tournamentKey: String) {
        currentUserRef
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .removeEventListener(teamsListener)

        queriedLiveData.removeObserver(queryOberver)
    }
}