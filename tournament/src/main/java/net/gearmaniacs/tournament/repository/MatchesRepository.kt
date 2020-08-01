package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.*
import net.gearmaniacs.core.model.Match
import net.theluckycoder.database.dao.MatchesDao

internal class MatchesRepository(
    private val matchesDao: MatchesDao,
    private val tournamentReference: DatabaseReference?
) {

    private var userTeamNumber = -1
    private var valueEventListenerJob: Job? = null

    fun getMatchesFlow(tournamentKey: String) = matchesDao.getAllByTournament(tournamentKey)

    val infoLiveData = MutableNonNullLiveData(emptyList<Match>())

    private fun updateInfoData(list: List<Match>) {
        if (userTeamNumber == -1)
            infoLiveData.postValue(emptyList())

        val filteredList = list.filter { match -> match.containsTeam(userTeamNumber) }
        infoLiveData.postValue(filteredList)
    }

    suspend fun addMatch(tournamentKey: String, match: Match) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
        }

        insertMatch(match, tournamentKey, ref)
    }

    suspend fun addMatches(tournamentKey: String, matches: List<Match>) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
        }

        matches.forEach {
            insertMatch(it, tournamentKey, ref)
        }
    }

    private suspend fun insertMatch(match: Match, tournamentKey: String, ref: DatabaseReference?) {
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

    suspend fun addListener(tournamentKey: String) = coroutineScope {
        justTry { valueEventListenerJob?.cancelAndJoin() }

        if (!Firebase.isLoggedIn) return@coroutineScope

        valueEventListenerJob = launch {
            val databaseReference = tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)

            databaseReference.listValueEventListenerFlow(Match::class).safeCollect {
                matchesDao.replaceTournamentMatches(tournamentKey, it)
                updateInfoData(it)
            }
        }
    }

    fun removeListener() {
        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }
}
