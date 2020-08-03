package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.generatePushId
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.listValueEventListenerFlow
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.theluckycoder.database.dao.MatchesDao
import javax.inject.Inject

internal class MatchesRepository @Inject constructor(
    @TournamentActivity.TournamentKey
    private val tournamentKey: String,
    private val matchesDao: MatchesDao,
    private val tournamentReference: DatabaseReference?
) {

    private var userTeamNumber = -1

    private var matchesFlowCollector: Job? = null
    private var valueEventListenerJob: Job? = null

    val matchesFlow = matchesDao.getAllByTournament(tournamentKey)
    val infoData = MutableNonNullLiveData(emptyList<Match>())

    private fun updateInfoData(list: List<Match>) {
        if (userTeamNumber == -1)
            infoData.postValue(emptyList())

        val filteredList = list.filter { match -> match.containsTeam(userTeamNumber) }
        infoData.postValue(filteredList)
    }

    suspend fun addMatch(match: Match) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
        }

        insertMatch(match, ref)
    }

    suspend fun addMatches(matches: List<Match>) {
        val ref = Firebase.ifLoggedIn {
            tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)
        }

        matches.forEach {
            insertMatch(it, ref)
        }
    }

    private suspend fun insertMatch(match: Match, ref: DatabaseReference?) {
        val key = if (ref != null) {
            val matchRef = ref.push()
            matchRef.setValue(match)
            matchRef.key!!
        } else {
            Firebase.generatePushId()
        }

        matchesDao.insert(match.copy(key = key, tournamentKey = tournamentKey))
    }

    suspend fun updateMatch(match: Match) {
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

    suspend fun deleteMatch(matchKey: String) {
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

    suspend fun addListener() = coroutineScope {
        justTry { matchesFlowCollector?.cancelAndJoin() }
        justTry { valueEventListenerJob?.cancelAndJoin() }

        matchesFlowCollector = launch {
            matchesFlow.collectLatest {
                coroutineContext.ensureActive()

                updateInfoData(it)
            }
        }

        if (!Firebase.isLoggedIn) return@coroutineScope

        valueEventListenerJob = launch {
            val databaseReference = tournamentReference!!
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)

            databaseReference.listValueEventListenerFlow(Match::class).safeCollect {
                matchesDao.replaceTournamentMatches(tournamentKey, it)
            }
        }
    }

    fun removeListener() {
        matchesFlowCollector?.cancel()
        matchesFlowCollector = null

        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }
}
