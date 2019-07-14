package net.gearmaniacs.ftcscouting.ui.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import net.gearmaniacs.ftcscouting.data.Alliance
import net.gearmaniacs.ftcscouting.data.Match
import net.gearmaniacs.ftcscouting.data.Team
import net.gearmaniacs.ftcscouting.data.TeamPower
import net.gearmaniacs.ftcscouting.data.User
import net.gearmaniacs.ftcscouting.opr.PowerRanking
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.InfoFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.TeamsFragment
import net.gearmaniacs.ftcscouting.utils.ChildListener
import net.gearmaniacs.ftcscouting.utils.SingleValueListener
import net.gearmaniacs.ftcscouting.utils.architecture.MutexLiveData
import net.gearmaniacs.ftcscouting.utils.architecture.NonNullLiveData
import net.gearmaniacs.ftcscouting.utils.extensions.toast

class TournamentViewModel : ViewModel() {

    private var initialized = false
    private var listening = false
    var tournamentKey = ""
        set(value) {
            stopListening()
            field = value
            startListening()
        }
    val currentUserReference by lazy {
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    }
    var fragmentTag = InfoFragment.TAG

    val nameData = MutableLiveData("")
    val teamsData = MutexLiveData(emptyList<Team>())
    val matchesData = MutexLiveData(emptyList<Match>())
    val analyticsData = NonNullLiveData(emptyList<TeamPower>())

    // Listeners
    private val nameChangeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            nameData.value = snapshot.getValue(String::class.java)
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }
    private val teamsListener = ChildListener(Team::class.java, teamsData, viewModelScope)
    private val matchesListener = ChildListener(Match::class.java, matchesData, viewModelScope)

    fun setDefaultName(defaultName: String) {
        if (nameData.value.isNullOrEmpty())
            nameData.value = defaultName
    }

    fun startListening() {
        if (listening) return

        currentUserReference.child("tournaments")
            .child(tournamentKey)
            .addValueEventListener(nameChangeListener)

        val tournamentRef = currentUserReference
            .child("data")
            .child(tournamentKey)
        val teamsRef = tournamentRef.child("teams")
        val matchesRef = tournamentRef.child("matches")

        teamsRef.addChildEventListener(teamsListener)
        matchesRef.addChildEventListener(matchesListener)

        if (!initialized) {
            teamsRef.addListenerForSingleValueEvent(SingleValueListener(Team::class.java, teamsData, viewModelScope))
            matchesRef
                .addListenerForSingleValueEvent(SingleValueListener(Match::class.java, matchesData, viewModelScope))
            initialized = true
        }

        listening = true
    }

    fun stopListening() {
        if (!listening) return

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
            .removeEventListener(teamsListener)

        listening = false
    }

    fun addTeams(teamIds: List<Int>) {
        viewModelScope.launch(Dispatchers.Default) {
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
    }

    fun calculateOpr(appContext: Context) {
        val teams = teamsData.value
        val matches = matchesData.value

        if (teams.isEmpty() || matches.isEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            val redAlliances = ArrayList<Alliance>(matches.size)
            val blueAlliances = ArrayList<Alliance>(matches.size)

            matches.forEach {
                redAlliances.add(it.redAlliance)
                blueAlliances.add(it.blueAlliance)
            }

            try {
                val powerRankings = PowerRanking(teams, redAlliances, blueAlliances).generatePowerRankings()

                analyticsData.postValue(powerRankings)
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    appContext.toast("Something went wrong. Please check the input data")
                }
            }
        }
    }

    override fun onCleared() {
        stopListening()
    }
}
