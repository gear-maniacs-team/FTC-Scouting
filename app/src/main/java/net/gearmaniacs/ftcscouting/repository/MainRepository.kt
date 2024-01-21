package net.gearmaniacs.ftcscouting.repository

import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventFlow
import net.gearmaniacs.core.firebase.valueEventFlow
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.core.utils.UserTeamPreferences
import net.gearmaniacs.database.dao.TeamsDao
import net.gearmaniacs.database.dao.TournamentsDao
import net.gearmaniacs.database.model.Tournament
import net.gearmaniacs.database.model.team.Team
import javax.inject.Inject

@ViewModelScoped
class MainRepository @Inject constructor(
    private val tournamentsDao: TournamentsDao,
    private val teamsDao: TeamsDao,
    private val userDataPreferences: UserTeamPreferences
) {

    private val tournamentsReference by lazy {
        Firebase.ifLoggedIn {
            Firebase.database
                .getReference(DatabasePaths.KEY_UNIVERSAL)
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

    val tournamentsFlow = tournamentsDao.getAllFlow().distinctUntilChanged()

    suspend fun createNewTournament(userTeam: UserTeam?, tournamentName: String) {
        val tournamentKey = if (Firebase.isLoggedIn) {
            val ref = tournamentsReference!!
                .child(DatabasePaths.KEY_TOURNAMENTS)
                .push()
            ref.setValue(tournamentName)
            ref.key!!
        } else
            Firebase.generatePushId()

        tournamentsDao.insert(Tournament(tournamentKey, tournamentName))

        if (!userTeam.isNullOrEmpty()) {
            val teamKey = Firebase.generatePushId()
            val team = Team(teamKey, tournamentKey, userTeam.id, userTeam.teamName)

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

    suspend fun startListener() = coroutineScope {
        if (!Firebase.isLoggedIn) return@coroutineScope

        launch {
            userReference!!.valueEventFlow<UserTeam>().safeCollect {
                if (it != null)
                    userDataPreferences.updateUserTeam(it)
            }
        }

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
