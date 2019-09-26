package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseChildListener
import net.gearmaniacs.core.firebase.FirebaseSingleValueListener
import net.gearmaniacs.core.model.Match

internal class MatchesRepository(private val tournamentReference: DatabaseReference) {

    val liveData = MutexLiveData(emptyList<Match>())

    private val matchesListener = FirebaseChildListener(Match::class.java, liveData)
    private var listenersInitialized = false

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

    fun addListeners(tournamentKey: String) {
        val tournamentRef = tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)

        val matchesRef = tournamentRef.child(DatabasePaths.KEY_MATCHES)

        if (!listenersInitialized) {
            matchesRef.addListenerForSingleValueEvent(
                FirebaseSingleValueListener(Match::class.java, liveData)
            )
            listenersInitialized = true
        }

        matchesRef.addChildEventListener(matchesListener)
    }

    fun removeListeners(tournamentKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .child(DatabasePaths.KEY_MATCHES)
            .removeEventListener(matchesListener)
    }
}