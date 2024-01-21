package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventFlow
import net.gearmaniacs.database.model.match.Match
import net.gearmaniacs.database.dao.MatchesDao
import javax.inject.Inject

internal class MatchesRepository @Inject constructor(
    private val matchesDao: MatchesDao,
    private val tournamentReference: DatabaseReference?
) {

    private var userTeamNumber = -1

    fun getMatchesFlow(tournamentKey: String) =
        matchesDao.getAllByTournament(tournamentKey)

    private val _infoFlow = MutableStateFlow(emptyList<Match>())
    val infoFlow = _infoFlow.asStateFlow()

    private fun updateInfoData(list: List<Match>) {
        if (userTeamNumber == -1)
            _infoFlow.value = emptyList()

        val filteredList = list.filter { match -> match.containsTeam(userTeamNumber) }
        _infoFlow.value = filteredList
    }

    suspend fun addMatch(tournamentKey: String, match: Match) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
        }

        insertMatch(tournamentKey, match, ref)
    }

    suspend fun addMatches(tournamentKey: String, matches: List<Match>) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
        }

        matches.forEach {
            insertMatch(tournamentKey, it, ref)
        }
    }

    private suspend fun insertMatch(tournamentKey: String, match: Match, ref: DatabaseReference?) {
        val key = if (ref != null) {
            val matchRef = ref.push()
            matchRef.setValue(match)
            matchRef.key!!
        } else {
            Firebase.generatePushId()
        }

        matchesDao.insert(match.copy(key = key, tournamentKey = tournamentKey))
    }

    suspend fun updateMatch(tournamentKey: String, match: Match) {
        matchesDao.insert(match)

        if (Firebase.isLoggedIn) {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
                .child(match.key)
                .setValue(match)
                .await()
        }
    }

    suspend fun deleteMatch(tournamentKey: String, matchKey: String) {
        matchesDao.delete(matchKey)

        if (Firebase.isLoggedIn) {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
                .child(matchKey)
                .removeValue()
                .await()
        }
    }

    fun setUserTeamNumber(number: Int) {
        userTeamNumber = number
    }

    suspend fun startListener(tournamentKey: String) = coroutineScope {
        launch {
            getMatchesFlow(tournamentKey).collectLatest {
                coroutineContext.ensureActive()

                updateInfoData(it)
            }
        }

        if (Firebase.isLoggedIn) {
            val databaseReference = tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)

            databaseReference.listValueEventFlow(Match::class).safeCollect {
                matchesDao.replaceTournamentMatches(tournamentKey, it)
            }
        }
    }
}
