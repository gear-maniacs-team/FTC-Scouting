package net.gearmaniacs.tournament.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.valueEventListenerFlow
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.tournament.opr.PowerRanking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

internal class TournamentRepository(private val tournamentReference: DatabaseReference) {

    private var valueEventListenerJob: Job? = null

    val nameLiveData = MutableLiveData<String>()

    suspend fun updateTournamentName(tournamentKey: String, newName: String) {
        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .setValue(newName)
            .await()
    }

    suspend fun deleteTournament(tournamentKey: String) {
        tournamentReference
            .child(DatabasePaths.KEY_TOURNAMENTS)
            .child(tournamentKey)
            .removeValue()
            .await()

        tournamentReference
            .child(DatabasePaths.KEY_DATA)
            .child(tournamentKey)
            .removeValue()
            .await()
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

    suspend fun addListener(tournamentKey: String) = coroutineScope {
        justTry { valueEventListenerJob?.cancelAndJoin() }

        valueEventListenerJob = launch {
            val databaseReference = tournamentReference
                .child(DatabasePaths.KEY_TOURNAMENTS)
                .child(tournamentKey)

            databaseReference.valueEventListenerFlow<String>().safeCollect {
                nameLiveData.postValue(it)
            }
        }
    }

    fun removeListener() {
        valueEventListenerJob?.cancel()
        valueEventListenerJob = null
    }
}
