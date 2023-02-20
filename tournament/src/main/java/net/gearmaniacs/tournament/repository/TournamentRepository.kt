package net.gearmaniacs.tournament.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.safeCollect
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.firebase.valueEventFlow
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.opr.OffensivePowerRanking
import net.theluckycoder.database.dao.TournamentsDao
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import javax.inject.Inject

internal class TournamentRepository @Inject constructor(
    private val tournamentsDao: TournamentsDao,
    private val tournamentReference: DatabaseReference?
) {

    fun getTournament(tournamentKey: String) =
        tournamentsDao.getFlow(tournamentKey)

    suspend fun updateTournamentName(tournamentKey: String, tournamentName: String) {
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

    suspend fun deleteTournament(tournamentKey: String) {
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

    suspend fun generateOprList(teams: List<Team>, matches: List<Match>): List<RankedTeam> {
        val decimalFormat = DecimalFormat("#.#")
        decimalFormat.decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = '.'
        }

        val rankings = OffensivePowerRanking.computeMMSE(matches, teams) ?: emptyList()

        // Format the score of each Team to only keep the first to decimals
        return rankings.map {
            it.copy(score = decimalFormat.format(it.score).toDouble())
        }
    }

    suspend fun startListener(tournamentKey: String) {
        if (!Firebase.isLoggedIn) return

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
