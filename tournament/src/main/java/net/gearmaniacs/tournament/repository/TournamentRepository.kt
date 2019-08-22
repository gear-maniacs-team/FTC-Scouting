package net.gearmaniacs.tournament.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseChildListener
import net.gearmaniacs.core.firebase.FirebaseDatabaseRepositoryCallback
import net.gearmaniacs.core.firebase.FirebaseSingleValueListener
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.tournament.opr.PowerRanking

internal class TournamentRepository(private val coroutineScope: CoroutineScope) {

    private val currentUserReference by lazy {
        FirebaseDatabase.getInstance()
            .reference
            .child(DatabasePaths.KEY_USERS)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val nameChangeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                nameCallback?.onSuccess(snapshot.getValue(String::class.java))
            } catch (e: Exception) {
                nameCallback?.onError(e)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            nameCallback?.onError(error.toException())
        }
    }

    val teamsData = MutexLiveData(emptyList<Team>())
    val filteredTeamsData = NonNullLiveData(emptyList<Team>())
    val matchesData = MutexLiveData(emptyList<Match>())

    private val teamsListener = FirebaseChildListener(Team::class.java, teamsData, coroutineScope)
    private val matchesListener = FirebaseChildListener(Match::class.java, matchesData, coroutineScope)

    private var listenersInitialized = false

    var nameCallback: FirebaseDatabaseRepositoryCallback<String?>? = null
    private var lastTeamQuery = ""
    private var teamSearchJob: Job? = null // Last launched search job

    init {
        teamsData.observeForever {
            performTeamsSearch(lastTeamQuery)
        }
    }

    fun addTeam(tournamentKey: String, team: Team) {
        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .push()
            .setValue(team)
    }

    fun addTeams(tournamentKey: String, teamIds: List<Team>) {
        val ref = currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)

        teamIds.forEach {
            ref.push().setValue(it)
        }
    }

    fun updatedTeam(tournamentKey: String, team: Team) {
        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .child(team.key!!)
            .setValue(team)
    }

    fun deleteTeam(tournamentKey: String, teamKey: String) {
        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .child(teamKey)
            .removeValue()
    }

    fun performTeamsSearch(query: String) {
        lastTeamQuery = query
        val teamList = teamsData.value

        if (teamList.isEmpty() || query.isEmpty()) {
            filteredTeamsData.value = teamList
            return
        }

        // Cancel the last running job and start a new one
        teamSearchJob?.cancel()
        teamSearchJob = coroutineScope.launch(Dispatchers.Default) {
            val filteredList = ArrayList<Team>(teamList.size)
            val pattern = "(?i).*($query).*".toPattern()

            teamList.asSequence()
                .filter { pattern.matcher(it.name.orEmpty()).matches() }
                .forEach { filteredList.add(it) }

            launch(Dispatchers.Main) {
                filteredTeamsData.value = filteredList
            }
        }
    }


    fun addMatch(tournamentKey: String, match: Match) {
        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child("matches")
            .push()
            .setValue(match)
    }

    fun addMatches(tournamentKey: String, matches: List<Match>) {
        val ref = currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)

        matches.forEach {
            ref.push().setValue(it)
        }
    }

    fun updatedMatch(tournamentKey: String, match: Match) {
        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .child(match.key!!)
            .setValue(match)
    }

    fun deleteMatch(tournamentKey: String, matchKey: String) {
        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .child(matchKey)
            .removeValue()
    }


    fun updateTournamentName(tournamentKey: String, newName: String) {
        currentUserReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .setValue(newName)
    }

    fun deleteTournament(tournamentKey: String) {
        currentUserReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeValue()

        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .removeValue()
    }

    suspend fun generateOprList(): List<TeamPower> {
        val teams = teamsData.value
        val matches = matchesData.value

        val redAlliances = ArrayList<Alliance>(matches.size)
        val blueAlliances = ArrayList<Alliance>(matches.size)

        matches.forEach {
            redAlliances.add(it.redAlliance)
            blueAlliances.add(it.blueAlliance)
        }

        return try {
            PowerRanking(teams, redAlliances, blueAlliances).generatePowerRankings()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun addListeners(tournamentKey: String) {
        currentUserReference.child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .addValueEventListener(nameChangeListener)

        val tournamentRef = currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)

        val teamsRef = tournamentRef.child(DatabasePaths.KEY_TEAMS)
        val matchesRef = tournamentRef.child(DatabasePaths.KEY_MATCHES)

        if (!listenersInitialized) {
            teamsRef.addListenerForSingleValueEvent(
                FirebaseSingleValueListener(
                    Team::class.java,
                    teamsData,
                    coroutineScope
                )
            )
            matchesRef.addListenerForSingleValueEvent(
                FirebaseSingleValueListener(
                    Match::class.java,
                    matchesData,
                    coroutineScope
                )
            )
            listenersInitialized = true
        }

        teamsRef.addChildEventListener(teamsListener)
        matchesRef.addChildEventListener(matchesListener)
    }

    fun removeListeners(tournamentKey: String) {
        currentUserReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeEventListener(nameChangeListener)

        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_TEAMS)
            .removeEventListener(teamsListener)

        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .removeEventListener(matchesListener)
    }
}
