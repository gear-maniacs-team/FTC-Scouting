package net.gearmaniacs.tournament.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.extensions.app
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.csv.CsvExport
import net.gearmaniacs.tournament.csv.CsvImport
import net.gearmaniacs.tournament.repository.MatchesRepository
import net.gearmaniacs.tournament.repository.TeamsRepository
import net.gearmaniacs.tournament.repository.TournamentRepository
import java.nio.charset.Charset
import javax.inject.Inject

@HiltViewModel
internal class TournamentViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val teamsRepository: TeamsRepository,
    private val matchesRepository: MatchesRepository,
    application: Application
) : AndroidViewModel(application) {

    var tournamentKey = ""

    private val _leaderboardFlow = MutableStateFlow(emptyList<RankedTeam>())
    val leaderboardFlow = _leaderboardFlow.asStateFlow()
    val tournamentFlow by lazy {
        tournamentRepository.getTournament(tournamentKey).distinctUntilChanged()
    }
    val teamsFlow by lazy {
        teamsRepository.getTeamsFlow(tournamentKey).distinctUntilChanged()
    }
    val matchesFlow by lazy {
        matchesRepository.getMatchesFlow(tournamentKey).distinctUntilChanged()
    }

    val showAppBar = mutableStateOf(true)

    private var listening = false

    fun getInfoFlow(userTeam: UserTeam): StateFlow<List<Match>> {
        matchesRepository.setUserTeamNumber(userTeam.id)
        return matchesRepository.infoFlow
    }

    // region Teams Management

    fun addTeamsFromMatches() {
        viewModelScope.launch(Dispatchers.Default) {
            val matches = matchesFlow.first()
            val existingTeamIds = teamsFlow.first().map { it.number }
            val teamIds = HashSet<Int>(matches.size)

            matches.forEach {
                teamIds.add(it.redAlliance.firstTeam)
                teamIds.add(it.redAlliance.secondTeam)
                teamIds.add(it.blueAlliance.firstTeam)
                teamIds.add(it.blueAlliance.secondTeam)
            }

            val newTeamsList = teamIds.asSequence()
                .filter { it > 0 }
                .filterNot { existingTeamIds.contains(it) }
                .map { Team(key = "", number = it) }
                .toList()

            teamsRepository.addTeams(tournamentKey, newTeamsList)
        }
    }

    fun updateTeam(team: Team) = viewModelScope.launch(Dispatchers.IO) {
        val key = team.key

        if (key.isEmpty())
            teamsRepository.addTeam(tournamentKey, team)
        else
            teamsRepository.updateTeam(tournamentKey, team)
    }

    fun deleteTeam(teamKey: String) = viewModelScope.launch(Dispatchers.IO) {
        teamsRepository.deleteTeam(tournamentKey, teamKey)
    }

    // endregion

    // region Match Management

    fun updateMatch(match: Match) = viewModelScope.launch(Dispatchers.IO) {
        val key = match.key

        if (key.isEmpty())
            matchesRepository.addMatch(tournamentKey, match)
        else
            matchesRepository.updateMatch(tournamentKey, match)
    }

    fun deleteMatch(matchKey: String) = viewModelScope.launch(Dispatchers.IO) {
        matchesRepository.deleteMatch(tournamentKey, matchKey)
    }

    // endregion

    // region Tournament Management

    fun updateTournamentName(newName: String) = viewModelScope.launch(Dispatchers.IO) {
        if (newName.isNotBlank())
            tournamentRepository.updateTournamentName(tournamentKey, newName)
    }

    fun deleteTournament() = viewModelScope.launch(Dispatchers.IO) {
        tournamentRepository.deleteTournament(tournamentKey)
    }

    // endregion

    suspend fun refreshLeaderboardData(teams: List<Team>, matches: List<Match>) =
        withContext(Dispatchers.Default) {
            if (matches.isEmpty())
                return@withContext app.getString(R.string.opr_error_no_matches)

            val powerRankings = tournamentRepository.generateOprList(teams, matches)

            _leaderboardFlow.value = powerRankings

            if (powerRankings.isEmpty())
                app.getString(R.string.opr_error_data)
            else
                ""
        }

    // region Spreadsheet

    private fun exportCsv(fileUri: Uri, csv: String) {
        val message = try {
            app.contentResolver.openOutputStream(fileUri)!!.buffered().use {
                it.write(csv.toByteArray())
            }

            R.string.csv_saved_successfully
        } catch (e: Exception) {
            e.printStackTrace()

            R.string.spreadsheet_error
        }

        viewModelScope.launch(Dispatchers.Main) {
            app.toast(message)
        }
    }

    fun exportTeamsToCsv(fileUri: Uri) =
        viewModelScope.launch(Dispatchers.IO) {
            val teams = teamsFlow.first()
            val csv = CsvExport.exportTeams(teams)
            exportCsv(fileUri, csv)
        }

    fun exportMatchesToCsv(fileUri: Uri) =
        viewModelScope.launch(Dispatchers.IO) {
            val matches = matchesFlow.first()
            val csv = CsvExport.exportMatches(matches)
            exportCsv(fileUri, csv)
        }

    fun exportOprToCsv(fileUri: Uri) =
        viewModelScope.launch(Dispatchers.IO) {
            val csv = CsvExport.exportOpr(leaderboardFlow.value)
            exportCsv(fileUri, csv)
        }

    fun importTeamsFromCsv(fileUri: Uri) =
        viewModelScope.launch(Dispatchers.IO) {
            val currentTeams = teamsFlow.first()

            app.contentResolver.openInputStream(fileUri)?.buffered()?.use { inputStream ->
                val csv = inputStream.readBytes().toString(Charset.defaultCharset())
                val importedTeams = CsvImport.importTeams(csv)

                teamsRepository.addTeams(
                    tournamentKey,
                    importedTeams.filterNot { currentTeams.contains(it) })
            }
        }

    fun importMatchesFromCsv(fileUri: Uri) =
        viewModelScope.launch(Dispatchers.IO) {
            val currentMatches = matchesFlow.first()

            app.contentResolver.openInputStream(fileUri)?.buffered()?.use { inputStream ->
                val csv = inputStream.readBytes().toString(Charset.defaultCharset())
                val importedMatches = CsvImport.importMatch(csv)

                matchesRepository.addMatches(
                    tournamentKey,
                    importedMatches.filterNot { currentMatches.contains(it) })
            }
        }

    // endregion

    fun startListening() {
        if (listening) return
        listening = true

        viewModelScope.launch(Dispatchers.IO) { tournamentRepository.startListener(tournamentKey) }
        viewModelScope.launch(Dispatchers.IO) { teamsRepository.startListener(tournamentKey) }
        viewModelScope.launch(Dispatchers.IO) { matchesRepository.startListener(tournamentKey) }
    }
}
