package net.gearmaniacs.ftcscouting.repository

import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventFlow
import net.gearmaniacs.core.firebase.valueEventFlow
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.core.utils.AppPreferences
import net.theluckycoder.database.dao.TeamsDao
import net.theluckycoder.database.dao.TournamentsDao
import javax.inject.Inject

@ActivityRetainedScoped
class MainRepository @Inject constructor(
    private val tournamentsDao: TournamentsDao,
    private val teamsDao: TeamsDao,
    private val appPreferences: AppPreferences
) {

    private var listenerScope: CoroutineScope? = null

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

    val userDataFlow = appPreferences.userDataNumber.asFlow()
        .combine(appPreferences.userDataName.asFlow()) { id: Int, name: String ->
            UserData(id, name)
        }
    val tournamentsFlow = tournamentsDao.getAllFlow()

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

        if (!userData.isNullOrEmpty()) {
            val teamKey = Firebase.generatePushId()
            val team = Team(teamKey, tournamentKey, userData.id, userData.teamName)

            teamsDao.insert(team)

            if (Firebase.isLoggedIn) {
                tournamentsReference!!
                    .child(DatabasePaths.KEY_DATA)
                    .child(tournamentKey)
                    .child(DatabasePaths.KEY_TEAMS)
                    .child(teamKey)
                    .setValue(team)
                    .await()
            }
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

    suspend fun addListener() = coroutineScope {
        removeListener()
        listenerScope = this

        if (!Firebase.isLoggedIn) return@coroutineScope

        launch {
            userReference!!.valueEventFlow<UserData>().safeCollect {
                if (it != null) {
                    appPreferences.userDataNumber.setAndCommit(it.id)
                    appPreferences.userDataName.setAndCommit(it.teamName)
                }
            }
        }

        launch {
            tournamentsReference!!
                .child(DatabasePaths.KEY_TOURNAMENTS)
                .listValueEventFlow { snapshot ->
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

    fun removeListener() {
        justTry { listenerScope?.cancel() }
        listenerScope = null
    }
}
