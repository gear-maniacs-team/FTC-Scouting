package net.gearmaniacs.tournament.repository

import androidx.lifecycle.Observer
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseChildListener
import net.gearmaniacs.core.firebase.FirebaseSingleValueListener
import net.gearmaniacs.core.model.Match

internal class MatchesRepository(private val tournamentReference: DatabaseReference) {

    val matchesLiveData = MutexLiveData(emptyList<Match>())
    val infoLiveData = MutexLiveData(emptyList<Match>())

    private val infoObserver = Observer<List<Match>>(::updateInfoData)
    private val matchesListener = FirebaseChildListener(Match::class.java, matchesLiveData)
    private var listenersInitialized = false
    private var userTeamNumber = -1

    private fun updateInfoData(list: List<Match>) {
        if (userTeamNumber == -1)
            infoLiveData.value = emptyList()

        GlobalScope.launch(Dispatchers.Main.immediate) {
            val filtered = withContext(Dispatchers.Default) {
                list.filter { match -> match.containsTeam(userTeamNumber) }
            }
            infoLiveData.value = filtered
        }
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

    fun addListeners(tournamentKey: String) {
        val tournamentRef = tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)

        val matchesRef = tournamentRef.child(DatabasePaths.KEY_MATCHES)

        if (!listenersInitialized) {
            matchesRef.addListenerForSingleValueEvent(
                FirebaseSingleValueListener(Match::class.java, matchesLiveData)
            )
            listenersInitialized = true
        }

        matchesRef.addChildEventListener(matchesListener)

        matchesLiveData.observeForever(infoObserver)
    }

    fun removeListeners(tournamentKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .removeEventListener(matchesListener)

        matchesLiveData.removeObserver(infoObserver)
    }
}