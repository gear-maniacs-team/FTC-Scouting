package net.gearmaniacs.ftcscouting.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import net.gearmaniacs.ftcscouting.model.Match
import net.gearmaniacs.ftcscouting.model.Team
import net.gearmaniacs.ftcscouting.utils.architecture.MutexLiveData
import net.gearmaniacs.ftcscouting.utils.firebase.FirebaseChildListener
import net.gearmaniacs.ftcscouting.utils.firebase.FirebaseDatabaseRepositoryCallback
import net.gearmaniacs.ftcscouting.utils.firebase.FirebaseSingleValueListener

class TournamentRepository(
    private val coroutineScope: CoroutineScope,
    private val teamsData: MutexLiveData<List<Team>>,
    private val matchesData: MutexLiveData<List<Match>>
) {

    private val currentUserReference by lazy {
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
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

    fun addTeams(tournamentKey: String, teamIds: List<Int>) {
        val ref = currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("teams")

        teamIds
            .filter { it != 0 }
            .distinct()
            .map { Team(it, "") }
            .forEach { ref.push().setValue(it) }
    }


    fun addTeam(tournamentKey: String, team: Team) {
        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("matches")
            .push()
            .setValue(team)
    }

    fun updatedTeam(tournamentKey: String, team: Team) {
        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("teams")
            .child(team.key!!)
            .setValue(team)
    }

    fun deleteTeam(tournamentKey: String, teamKey: String) {
        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("teams")
            .child(teamKey)
            .removeValue()
    }


    fun addMatch(tournamentKey: String, match: Match) {
        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("matches")
            .push()
            .setValue(match)
    }

    fun updatedMatch(tournamentKey: String, match: Match) {
        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("matches")
            .child(match.key!!)
            .setValue(match)
    }

    fun deleteMatch(tournamentKey: String, matchKey: String) {
        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("matches")
            .child(matchKey)
            .removeValue()
    }


    fun updateTournamentName(tournamentKey: String, newName: String) {
        currentUserReference
            .child("tournaments")
            .child(tournamentKey)
            .setValue(newName)
    }

    fun deleteTournament(tournamentKey: String) {
        currentUserReference
            .child("tournaments")
            .child(tournamentKey)
            .removeValue()

        currentUserReference
            .child("data")
            .child(tournamentKey)
            .removeValue()
    }

    fun addListeners(tournamentKey: String) {
        currentUserReference.child("tournaments")
            .child(tournamentKey)
            .addValueEventListener(nameChangeListener)

        val tournamentRef = currentUserReference
            .child("data")
            .child(tournamentKey)

        val teamsRef = tournamentRef.child("teams")
        val matchesRef = tournamentRef.child("matches")

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
            .child("tournaments")
            .child(tournamentKey)
            .removeEventListener(nameChangeListener)

        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("teams")
            .removeEventListener(teamsListener)

        currentUserReference
            .child("data")
            .child(tournamentKey)
            .child("matches")
            .removeEventListener(matchesListener)
    }
}
