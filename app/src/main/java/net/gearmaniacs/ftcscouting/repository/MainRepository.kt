package net.gearmaniacs.ftcscouting.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.auth
import net.gearmaniacs.core.firebase.listValueEventListenerFlow
import net.gearmaniacs.core.firebase.valueEventListenerFlow
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.theluckycoder.database.dao.TournamentsDao

class MainRepository(
    private val tournamentsDao: TournamentsDao
) {

    private val tournamentsReference by lazy {
        Firebase.database
            .getReference(DatabasePaths.KEY_SKYSTONE)
            .child(Firebase.auth.currentUser!!.uid)
    }
    private val userReference by lazy {
        Firebase.database
            .getReference(DatabasePaths.KEY_USERS)
            .child(Firebase.auth.currentUser!!.uid)
    }
    private var valueEventListenerJob: Job? = null

    val userLiveData = MutableLiveData<User>()
    val tournamentsFlow = tournamentsDao.getAll()

    suspend fun addListener() = coroutineScope {
        justTry { valueEventListenerJob?.cancelAndJoin() }

        valueEventListenerJob = launch {
            launch {
                userReference.valueEventListenerFlow<User>().safeCollect {
                    userLiveData.postValue(it)
                }
            }

            launch {
                tournamentsReference
                    .child(DatabasePaths.KEY_TOURNAMENTS)
                    .listValueEventListenerFlow { snapshot ->
                        val snapshotKey = snapshot.key
                        val name = snapshot.getValue<String>()

                        if (snapshotKey != null && name != null)
                            Tournament(snapshotKey, name)
                        else
                            null
                    }.safeCollect {
                        tournamentsDao.replaceAll(it)
                    }
            }
        }
    }

    fun removeListener() {
        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }

    suspend fun createNewTournament(user: User?, tournamentName: String) {
        val newTournament = tournamentsReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .push()

        newTournament.setValue(tournamentName)

        if (user != null) {
            val key = newTournament.key ?: return
            val team = Team("", "", user.id, user.teamName)

            tournamentsReference
                .child(DatabasePaths.KEY_DATA)
                .child(key)
                .child(DatabasePaths.KEY_TEAMS)
                .push()
                .setValue(team)
                .await()
        }
    }

    suspend fun deleteTournament(tournamentKey: String) {
        tournamentsReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeValue()
            .await()

        tournamentsReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .removeValue()
            .await()
    }
}
