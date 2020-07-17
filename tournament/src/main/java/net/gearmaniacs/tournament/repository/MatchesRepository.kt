package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.listValueEventListenerFlow
import net.gearmaniacs.core.model.Match

internal class MatchesRepository(private val tournamentReference: DatabaseReference) {

    private var userTeamNumber = -1
    private var valueEventListenerJob: Job? = null

    val infoLiveData = MutableNonNullLiveData(emptyList<Match>())
    val matchesLiveData = MutableNonNullLiveData(emptyList<Match>())

    private fun updateInfoData(list: List<Match>) {
        if (userTeamNumber == -1)
            infoLiveData.postValue(emptyList())

        val filteredList = list.filter { match -> match.containsTeam(userTeamNumber) }
        infoLiveData.postValue(filteredList)
    }

    fun addMatch(tournamentKey: String, match: Match) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .push()
            .setValue(match)
    }

    fun addMatches(tournamentKey: String, matches: List<Match>) {
        val ref = tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)

        matches.forEach {
            ref.push().setValue(it)
        }
    }

    fun updateMatch(tournamentKey: String, match: Match) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .child(match.key!!)
            .setValue(match)
    }

    fun deleteMatch(tournamentKey: String, matchKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .child(matchKey)
            .removeValue()
    }

    fun setUserTeamNumber(number: Int) {
        userTeamNumber = number
    }

    suspend fun addListener(tournamentKey: String) = coroutineScope {
        justTry { valueEventListenerJob?.cancelAndJoin() }

        valueEventListenerJob = launch {
            val databaseReference = tournamentReference
                .child(DatabasePaths.KEY_DATA)
                .child(tournamentKey)
                .child(DatabasePaths.KEY_MATCHES)

            databaseReference.listValueEventListenerFlow(Match::class.java).safeCollect {
                matchesLiveData.postValue(it)
                updateInfoData(it)
            }
        }
    }

    fun removeListener() {
        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }
}
