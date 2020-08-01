package net.gearmaniacs.tournament.viewmodel

import android.app.Application
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.extensions.app
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.model.*
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.repository.MatchesRepository
import net.gearmaniacs.tournament.repository.TeamsRepository
import net.gearmaniacs.tournament.repository.TournamentRepository
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetExport
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetImport
import java.util.Locale

internal class TournamentViewModel @ViewModelInject constructor(
    private val tournamentRepository: TournamentRepository,
    private val teamsRepository: TeamsRepository,
    private val matchesRepository: MatchesRepository,
    application: Application
) : AndroidViewModel(application) {

    private var listening = false
    var tournamentKey = ""

    private val analyticsData = MutableNonNullLiveData(emptyList<TeamPower>())
    private val currentTournamentData by lazy { tournamentRepository.getCurrentTournamentFlow(tournamentKey).asLiveData() }
    private val teamsData by lazy { teamsRepository.getTeamsFlow(tournamentKey).asLiveData() }
    private val matchesData by lazy { matchesRepository.getMatchesFlow(tournamentKey).asLiveData() }

    fun getInfoLiveData(userData: UserData): NonNullLiveData<List<Match>> {
        matchesRepository.setUserTeamNumber(userData.id)
        return matchesRepository.infoLiveData
    }

    fun getCurrentTournamentLiveData() = currentTournamentData

    fun getTeamsLiveData() = teamsData

    fun getTeamsFilteredLiveData(): NonNullLiveData<List<Team>> = teamsRepository.queriedLiveData

    fun getMatchesLiveData() = matchesData

    fun getAnalyticsLiveData(): NonNullLiveData<List<TeamPower>> = analyticsData

    // region Teams Management

    fun performTeamsSearch(query: String?) {
        teamsRepository.performTeamsSearch(query.orEmpty().trim().toLowerCase(Locale.ROOT))
    }

    fun addTeamsFromMatches(teams: List<Team>, matches: List<Match>) {
        val existingTeamIds = teams.map { it.id }

        viewModelScope.launch(Dispatchers.Default) {
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
                .map { Team("", tournamentKey, it, null) }
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
            tournamentRepository.updateTournamentName(Tournament(tournamentKey, newName))
    }

    fun deleteTournament() = viewModelScope.launch(Dispatchers.IO) {
        tournamentRepository.deleteTournament(tournamentKey)
    }

    // endregion

    suspend fun refreshAnalyticsData(teams: List<Team>, matches: List<Match>) =
        withContext(Dispatchers.Main.immediate) {
            if (matches.isEmpty())
                return@withContext app.getString(R.string.opr_error_no_matches)

            val powerRankings = withContext(Dispatchers.Default) {
                tournamentRepository.generateOprList(teams, matches)
            }

            analyticsData.value = powerRankings

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
                val importedTeams = import.getTeams().map { it.copy(tournamentKey = tournamentKey) }
                val importedMatches = import.getMatches().map { it.copy(tournamentKey = tournamentKey) }

                teamsRepository.addTeams(
                    tournamentKey,
                    importedTeams.filterNot { currentTeams.contains(it) }
                )
                matchesRepository.addMatches(
                    tournamentKey,
                    importedMatches.filterNot { currentMatches.contains(it) }
                )
            }
        }

    // endregion

    fun startListening() {
        if (listening) return
        listening = true

        viewModelScope.launch(Dispatchers.IO) {
            tournamentRepository.addListener(tournamentKey)
        }
        viewModelScope.launch(Dispatchers.IO) {
            matchesRepository.addListener(tournamentKey)
        }
        viewModelScope.launch(Dispatchers.IO) {
            teamsRepository.addListener(tournamentKey)
        }
    }

    fun stopListening() {
        if (!listening) return

        tournamentRepository.removeListener()
        teamsRepository.removeListener()
        matchesRepository.removeListener()

        listening = false
    }

    override fun onCleared() {
        stopListening()
    }
}
