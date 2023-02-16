package net.gearmaniacs.tournament.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.extensions.app
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.repository.MatchesRepository
import net.gearmaniacs.tournament.repository.TeamsRepository
import net.gearmaniacs.tournament.repository.TournamentRepository
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetExport
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetImport
import net.gearmaniacs.tournament.ui.adapter.TeamSearchAdapter
import javax.inject.Inject

@HiltViewModel
internal class TournamentViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val teamsRepository: TeamsRepository,
    private val matchesRepository: MatchesRepository,
    application: Application
) : AndroidViewModel(application) {

    private val leaderboardData = MutableNonNullLiveData(emptyList<RankedTeam>())
    private val tournamentData = tournamentRepository.tournamentFlow.asLiveData()
    private val teamsData = teamsRepository.teamsFlows.asLiveData()
    private val queriedTeamsData = teamsRepository.queriedTeamsFlow.asLiveData()
    private val matchesData = matchesRepository.matchesFlow.asLiveData()

    private var listening = false

    fun getInfoLiveData(userTeam: UserTeam): NonNullLiveData<List<Match>> {
        matchesRepository.setUserTeamNumber(userTeam.id)
        return matchesRepository.infoData
    }

    fun getCurrentTournamentLiveData() = tournamentData

    fun getTeamsLiveData() = teamsData

    fun getTeamsFilteredLiveData() = queriedTeamsData

    fun getMatchesLiveData() = matchesData

    fun getLeaderboardLiveData(): NonNullLiveData<List<RankedTeam>> = leaderboardData

    // region Teams Management

    fun queryTeams(query: TeamSearchAdapter.Query?) {
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

            teamsRepository.addTeams(newTeamsList)
        }
    }

    fun updateTeam(team: Team) = viewModelScope.launch(Dispatchers.IO) {
        val key = team.key

        if (key.isEmpty())
            teamsRepository.addTeam(team)
        else
            teamsRepository.updateTeam(team)
    }

    fun deleteTeam(teamKey: String) = viewModelScope.launch(Dispatchers.IO) {
        teamsRepository.deleteTeam(teamKey)
    }

    // endregion

    // region Match Management

    fun updateMatch(match: Match) = viewModelScope.launch(Dispatchers.IO) {
        val key = match.key

        if (key.isEmpty())
            matchesRepository.addMatch(match)
        else
            matchesRepository.updateMatch(match)
    }

    fun deleteMatch(matchKey: String) = viewModelScope.launch(Dispatchers.IO) {
        matchesRepository.deleteMatch(matchKey)
    }

    // endregion

    // region Tournament Management

    fun updateTournamentName(newName: String) = viewModelScope.launch(Dispatchers.IO) {
        if (newName.isNotBlank())
            tournamentRepository.updateTournamentName(newName)
    }

    fun deleteTournament() = viewModelScope.launch(Dispatchers.IO) {
        tournamentRepository.deleteTournament()
    }

    // endregion

    suspend fun refreshLeaderboardData(teams: List<Team>, matches: List<Match>) =
        withContext(Dispatchers.Main.immediate) {
            if (matches.isEmpty())
                return@withContext app.getString(R.string.opr_error_no_matches)

            val powerRankings = withContext(Dispatchers.Default) {
                tournamentRepository.generateOprList(teams, matches)
            }

            leaderboardData.value = powerRankings

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

                teamsRepository.addTeams(importedTeams.filterNot { currentTeams.contains(it) })
                matchesRepository.addMatches(importedMatches.filterNot { currentMatches.contains(it) })
            }
        }

    // endregion

    fun startListening() {
        if (listening) return
        listening = true

        viewModelScope.launch(Dispatchers.IO) { tournamentRepository.addListener() }
        viewModelScope.launch(Dispatchers.IO) { teamsRepository.addListener() }
        viewModelScope.launch(Dispatchers.IO) { matchesRepository.addListener() }
    }

    fun stopListening() {
        if (!listening) return

        viewModelScope.launch(Dispatchers.IO) { tournamentRepository.removeListener() }
        viewModelScope.launch(Dispatchers.IO) { teamsRepository.removeListener() }
        viewModelScope.launch(Dispatchers.IO) { matchesRepository.removeListener() }

        listening = false
    }

    override fun onCleared() {
        GlobalScope.launch(Dispatchers.IO) {
            tournamentRepository.clear()
            teamsRepository.clear()
            matchesRepository.clear()
        }
    }
}
