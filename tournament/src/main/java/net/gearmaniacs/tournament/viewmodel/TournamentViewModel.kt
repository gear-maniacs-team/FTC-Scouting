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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.extensions.app
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.repository.MatchesRepository
import net.gearmaniacs.tournament.repository.TeamsRepository
import net.gearmaniacs.tournament.repository.TournamentRepository
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetExport
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetImport
import net.gearmaniacs.tournament.ui.model.TeamSearchQuery
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
    val leaderBoardFlow = _leaderboardFlow.asStateFlow()
    val tournamentFlow by lazy {
        tournamentRepository.getTournament(tournamentKey).distinctUntilChanged()
    }
    val teamsFlow by lazy {
        teamsRepository.getTeamsFlow(tournamentKey).distinctUntilChanged()
    }
    val queriedTeamsFlow = teamsRepository.queriedTeamsFlow
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

    fun queryTeams(query: TeamSearchQuery) {
        teamsRepository.updateTeamsQuery(query)
    }

    fun addTeamsFromMatches(teams: List<Team>, matches: List<Match>) {
        viewModelScope.launch(Dispatchers.Default) {
            val existingTeamIds = teams.map { it.number }
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

    fun exportToSpreadsheet(fileUri: Uri, teams: List<Team>, matches: List<Match>) =
        viewModelScope.launch(Dispatchers.IO) {
            val powerRankings = tournamentRepository.generateOprList(teams, matches)

            try {
                val export = SpreadsheetExport()
                export.export(teams, matches, powerRankings)

                app.contentResolver.openOutputStream(fileUri)!!.use {
                    export.writeToStream(it)
                }

                launch(Dispatchers.Main) {
                    app.toast(R.string.spreadsheet_saved_successfully)
                }
            } catch (e: Exception) {
                e.printStackTrace()

                launch(Dispatchers.Main) {
                    app.toast(R.string.spreadsheet_error)
                }
            }
        }

    fun importFromSpreadSheet(fileUri: Uri, currentTeams: List<Team>, currentMatches: List<Match>) =
        viewModelScope.launch(Dispatchers.IO) {
            app.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                val import = SpreadsheetImport(inputStream)
                val importedTeams = import.getTeams()
                val importedMatches = import.getMatches()

                teamsRepository.addTeams(
                    tournamentKey,
                    importedTeams.filterNot { currentTeams.contains(it) })
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
