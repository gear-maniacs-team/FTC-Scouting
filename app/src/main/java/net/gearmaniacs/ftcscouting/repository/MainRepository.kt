package net.gearmaniacs.ftcscouting.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseDatabaseRepositoryCallback

class MainRepository(coroutineScope: CoroutineScope) {

    private val currentUserReference by lazy {
        FirebaseDatabase.getInstance()
            .reference
            .child(DatabasePaths.KEY_USERS)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val tournamentsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            coroutineScope.launch(Dispatchers.IO) {
                val list = snapshot.children.map {
                    Tournament(it.getValue(String::class.java)!!).apply {
                        this.key = it.key
                    }
                }.toMutableList()

                list.sort()

                launch(Dispatchers.Main) {
                    tournamentsCallback?.onSuccess(list)
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            tournamentsCallback?.onError(error.toException())
        }
    }

    private val userListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                val id = snapshot.child(User::id.name).getValue(Int::class.java) ?: return
                val teamName = snapshot.child(User::teamName.name).getValue(String::class.java).orEmpty()

                userCallback?.onSuccess(User(id, teamName))
            } catch (e: Exception) {
                userCallback?.onError(e)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            userCallback?.onError(error.toException())
        }
    }

    var tournamentsCallback: FirebaseDatabaseRepositoryCallback<List<Tournament>>? = null
    var userCallback: FirebaseDatabaseRepositoryCallback<User>? = null

    fun addListeners() {
        currentUserReference.addValueEventListener(userListener)

        currentUserReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .addValueEventListener(tournamentsListener)
    }

    fun removeListeners() {
        currentUserReference.addValueEventListener(userListener)

        currentUserReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .removeEventListener(tournamentsListener)
    }

    fun createNewTournament(user: User, tournamentName: String) {
        val newTournament = currentUserReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .push()

        newTournament.setValue(tournamentName)

        val key = newTournament.key ?: return
        val team = Team(user.id, user.teamName)

        currentUserReference
            .child(DatabasePaths.KEY_DATA)
            .child(key)
            .child(DatabasePaths.KEY_TEAMS)
            .push()
            .setValue(team)
    }
}
