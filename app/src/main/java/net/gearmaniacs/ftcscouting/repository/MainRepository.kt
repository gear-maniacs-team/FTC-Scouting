package net.gearmaniacs.ftcscouting.repository

import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.*
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.utils.AppPreferences
import net.theluckycoder.database.dao.TournamentsDao

class MainRepository(
    private val tournamentsDao: TournamentsDao,
    private val appPreferences: AppPreferences
) {

    private val tournamentsReference by lazy {
        Firebase.ifLoggedIn {
            Firebase.database
                .getReference(DatabasePaths.KEY_SKYSTONE)
                .child(it.uid)
        }
    }
    private val userReference by lazy {
        Firebase.ifLoggedIn {
            Firebase.database
                .getReference(DatabasePaths.KEY_USERS)
                .child(it.uid)
        }
    }
    private var valueEventListenerJob: Job? = null

    val userDataFlow = appPreferences.userDataNumberPref
        .asFlow()
        .combine(appPreferences.userDataNamePref.asFlow()) { id: Int, name: String ->
            UserData(id, name)
        }
    val tournamentsFlow = tournamentsDao.getAllFlow()

    suspend fun addListener() = coroutineScope {
        justTry { valueEventListenerJob?.cancelAndJoin() }

        if (!Firebase.isLoggedIn) return@coroutineScope

        valueEventListenerJob = launch {
            launch {
                userReference!!.valueEventListenerFlow<UserData>().safeCollect {
                    if (it != null) {
                        appPreferences.userDataNumberPref.setAndCommit(it.id)
                        appPreferences.userDataNamePref.setAndCommit(it.teamName)
                    }
                }
            }

            launch {
                tournamentsReference!!
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

    suspend fun createNewTournament(userData: UserData?, tournamentName: String) {
        val tournamentKey = if (Firebase.isLoggedIn) {
            val ref = tournamentsReference!!
                .child(DatabasePaths.KEY_TOURNAMENTS)
                .push()
            ref.setValue(tournamentName)
            ref.key!!
        } else
            Firebase.generatePushId()

        tournamentsDao.insert(Tournament(tournamentKey, tournamentName))

        if (Firebase.isLoggedIn && userData != null) {
            val team = Team("", tournamentKey, userData.id, userData.teamName)

            tournamentsReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_TEAMS)
                .push()
                .setValue(team)
                .await()
        }
    }

    suspend fun deleteTournament(tournamentKey: String) {
        tournamentsDao.delete(tournamentKey)

        if (Firebase.isLoggedIn) {
            tournamentsReference!!
                .child(DatabasePaths.KEY_TOURNAMENTS)
                .child(tournamentKey)
                .removeValue()
                .await()

            tournamentsReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .removeValue()
                .await()
        }
    }
}
