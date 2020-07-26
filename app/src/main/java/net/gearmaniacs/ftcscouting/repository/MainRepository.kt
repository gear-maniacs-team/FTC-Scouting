package net.gearmaniacs.ftcscouting.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.auth
import net.gearmaniacs.core.firebase.listValueEventListenerFlow
import net.gearmaniacs.core.firebase.valueEventListenerFlow
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User

class MainRepository {

    private val databaseReference = FirebaseDatabase.getInstance()
        .getReference(DatabasePaths.KEY_SKYSTONE)
        .child(Firebase.auth.currentUser!!.uid)
    private val userReference = FirebaseDatabase.getInstance()
        .getReference(DatabasePaths.KEY_USERS)
        .child(Firebase.auth.currentUser!!.uid)
    private var valueEventListenerJob: Job? = null

    val userLiveData = MutableLiveData<User>()
    val tournamentsLiveData = MutableNonNullLiveData(emptyList<Tournament>())

    suspend fun addListener() = coroutineScope {
        justTry { valueEventListenerJob?.cancelAndJoin() }

        valueEventListenerJob = launch {
            launch {
                userReference.valueEventListenerFlow<User>().safeCollect {
                    userLiveData.postValue(it)
                }
            }

            launch {
                databaseReference
                    .child(DatabasePaths.KEY_TOURNAMENTS)
                    .listValueEventListenerFlow { snapshot ->
                        val snapshotKey = snapshot.key
                        val name = snapshot.getValue<String>()

                        if (snapshotKey != null && name != null)
                            Tournament(snapshotKey, name)
                        else
                            null
                    }.safeCollect {
                        tournamentsLiveData.postValue(it)
                    }
            }
        }
    }

    fun removeListener() {
        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }

    suspend fun createNewTournament(user: User?, tournamentName: String) {
        val newTournament = databaseReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .push()

        newTournament.setValue(tournamentName)

        if (user != null) {
            val key = newTournament.key ?: return
            val team = Team("", "", user.id, user.teamName)

            databaseReference
                .child(DatabasePaths.KEY_DATA)
                .child(key)
                .child(DatabasePaths.KEY_TEAMS)
                .push()
                .setValue(team)
                .await()
        }
    }

    suspend fun deleteTournament(tournamentKey: String) {
        databaseReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeValue()
            .await()

        databaseReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .removeValue()
            .await()
    }
}
