package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseDatabaseCallback
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.tournament.opr.PowerRanking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

internal class TournamentRepository(private val tournamentReference: DatabaseReference) {

    private val nameChangeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                nameChangeCallback?.onSuccess(snapshot.getValue(String::class.java))
            } catch (e: Exception) {
                nameChangeCallback?.onError(e)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            nameChangeCallback?.onError(error.toException())
        }
    }

    var nameChangeCallback: FirebaseDatabaseCallback<String?>? = null

    fun updateTournamentName(tournamentKey: String, newName: String) {
        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .setValue(newName)
    }

    fun deleteTournament(tournamentKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeValue()

        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .removeValue()
    }

    suspend fun generateOprList(teams: List<Team>, matches: List<Match>): List<TeamPower> {
        return try {
            val decimalFormat = DecimalFormat("#.##")
            decimalFormat.decimalFormatSymbols = DecimalFormatSymbols().apply {
                decimalSeparator = '.'
            }

            val rankings = PowerRanking(teams, matches).generatePowerRankings()

            // Format the power of each Team to only keep the first to decimals
            rankings.forEach {
                it.power = decimalFormat.format(it.power).toFloat()
            }

            rankings
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun addListener(tournamentKey: String) {
        tournamentReference.child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .addValueEventListener(nameChangeListener)
    }

    fun removeListener(tournamentKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeEventListener(nameChangeListener)
    }
}
