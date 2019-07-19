package net.gearmaniacs.tournament.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseChildListener
import net.gearmaniacs.core.firebase.FirebaseDatabaseRepositoryCallback
import net.gearmaniacs.core.firebase.FirebaseSingleValueListener
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team

internal class TournamentRepository(
    private val coroutineScope: CoroutineScope,
    private val teamsData: MutexLiveData<List<Team>>,
    private val matchesData: MutexLiveData<List<Match>>
) {

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

    private val teamsListener =
        FirebaseChildListener(Team::class.java, teamsData, coroutineScope)
    private val matchesListener =
        FirebaseChildListener(Match::class.java, matchesData, coroutineScope)

    private var initialized = false

    var nameCallback: FirebaseDatabaseRepositoryCallback<String?>? = null

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

    fun addListeners(tournamentKey: String) {
        currentUserReference.child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .addValueEventListener(nameChangeListener)

        val tournamentRef = currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)

        val teamsRef = tournamentRef.child(DatabasePaths.KEY_TEAMS)
        val matchesRef = tournamentRef.child(DatabasePaths.KEY_MATCHES)

        if (!initialized) {
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
            initialized = true
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
