package net.gearmaniacs.ftcscouting.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseDatabaseCallback
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User

class MainRepository(
    coroutineScope: CoroutineScope,
    val userCallback: FirebaseDatabaseCallback<User>
) {

    private val tournamentReference = FirebaseDatabase.getInstance()
        .getReference(DatabasePaths.KEY_SKYSTONE)
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
    private val userReference = FirebaseDatabase.getInstance()
        .getReference(DatabasePaths.KEY_USERS)
        .child(FirebaseAuth.getInstance().currentUser!!.uid)

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
                    tournamentData.value = list
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            error.toException().printStackTrace()
        }
    }

    private val userListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                val user = snapshot.getValue(User::class.java)!!

                userCallback.onSuccess(user)
            } catch (e: Exception) {
                userCallback.onError(e)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            userCallback.onError(error.toException())
        }
    }

    val tournamentData = NonNullLiveData(emptyList<Tournament>())

    fun addListeners() {
        userReference.addValueEventListener(userListener)

        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .addValueEventListener(tournamentsListener)
    }

    fun removeListeners() {
        userReference.removeEventListener(userListener)

        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .removeEventListener(tournamentsListener)
    }

    fun createNewTournament(user: User?, tournamentName: String) {
        val newTournament = tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .push()

        newTournament.setValue(tournamentName)

        if (user != null) {
            val key = newTournament.key ?: return
            val team = Team(user.id, user.teamName)

            tournamentReference
                .child(DatabasePaths.KEY_DATA)
                .child(key)
                .child(DatabasePaths.KEY_TEAMS)
                .push()
                .setValue(team)
        }
    }

    fun deleteTournament(tournamentKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeValue()

        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .removeValue()
    }
}
