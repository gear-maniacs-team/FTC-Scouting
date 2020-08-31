package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.valueEventFlow
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.utils.AbstractListenerRepository
import net.gearmaniacs.tournament.opr.PowerRanking
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.theluckycoder.database.dao.TournamentsDao
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import javax.inject.Inject

internal class TournamentRepository @Inject constructor(
    @TournamentActivity.TournamentKey
    private val tournamentKey: String,
    private val tournamentsDao: TournamentsDao,
    private val tournamentReference: DatabaseReference?
) : AbstractListenerRepository() {

    val tournamentFlow = tournamentsDao.getFlow(tournamentKey)

    suspend fun updateTournamentName(tournamentName: String) {
        val tournament = Tournament(tournamentKey, tournamentName)
        tournamentsDao.update(tournament)

        if (Firebase.isLoggedIn) {
            tournamentReference!!
                .child(DatabasePaths.KEY_TOURNAMENTS)
                .child(tournament.key)
                .setValue(tournament.name)
                .await()
        }
    }

    suspend fun deleteTournament() {
        tournamentsDao.delete(tournamentKey)

        if (Firebase.isLoggedIn) {
            tournamentReference!!
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

    override suspend fun onListenerAdded(scope: CoroutineScope) {
        if (!Firebase.isLoggedIn) return

        scope.launch {
            val databaseReference = tournamentReference!!
                .child(DatabasePaths.KEY_TOURNAMENTS)
                .child(tournamentKey)

            databaseReference.valueEventFlow<String>().safeCollect { name ->
                if (name != null)
                    tournamentsDao.update(Tournament(tournamentKey, name))
                else
                    tournamentsDao.delete(tournamentKey)
            }
        }
    }
}
