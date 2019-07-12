package net.gearmaniacs.ftcscouting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import net.gearmaniacs.ftcscouting.data.Team
import net.gearmaniacs.ftcscouting.data.Tournament
import net.gearmaniacs.ftcscouting.data.User
import net.gearmaniacs.ftcscouting.utils.architecture.NonNullLiveData
import net.gearmaniacs.ftcscouting.utils.extensions.justTry

class MainViewModel : ViewModel() {

    private var listening = false
    private val currentUserReference by lazy {
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    }
    private val tournamentsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            viewModelScope.launch(Dispatchers.Default) {
                val list = snapshot.children.map {
                    Tournament(it.getValue(String::class.java)!!).apply {
                        this.key = it.key
                    }
                }.toMutableList()

                list.sort()

                launch(Dispatchers.Main) {
                    tournamentsData.value = list
                }
            }
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }
    private val userListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            justTry {
                val id = snapshot.child(User::id.name).getValue(Int::class.java) ?: return
                val teamName = snapshot.child(User::teamName.name).getValue(String::class.java).orEmpty()

                currentUser = User(id, teamName)
            }
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    val tournamentsData = NonNullLiveData(emptyList<Tournament>())
    var currentUser: User? = null

    fun startListening() {
        if (listening) return

        currentUserReference.addValueEventListener(userListener)

        currentUserReference
            .child("tournaments")
            .addValueEventListener(tournamentsListener)

        listening = true
    }

    fun stopListening() {
        if (!listening) return

        currentUserReference.addValueEventListener(userListener)

        currentUserReference
            .child("tournaments")
            .removeEventListener(tournamentsListener)

        listening = false
    }

    fun createNewTournament(tournamentName: String) {
        val newTournament = currentUserReference
            .child("tournaments")
            .push()

        newTournament.setValue(tournamentName)

        currentUser?.let {
            val key = newTournament.key ?: return
            val team = Team(it.id, it.teamName)

            currentUserReference
                .child("data")
                .child(key)
                .child("teams")
                .push()
                .setValue(team)
        }

    }

    override fun onCleared() {
        stopListening()
    }
}
