package net.gearmaniacs.tournament.viewmodel

import android.app.Application
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.gearmaniacs.core.architecture.MutableNonNullLiveData
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.extensions.app
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.core.model.User
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

    fun getInfoLiveData(user: User): NonNullLiveData<List<Match>> {
        matchesRepository.setUserTeamNumber(user.id)
        return matchesRepository.infoLiveData
    }

    fun getNameLiveData() = tournamentRepository.nameLiveData

    fun getTeamsLiveData(): NonNullLiveData<List<Team>> = teamsRepository.queriedLiveData

    fun getMatchesLiveData(): NonNullLiveData<List<Match>> = matchesRepository.matchesLiveData

    fun getAnalyticsLiveData(): NonNullLiveData<List<TeamPower>> = analyticsData

    fun setDefaultName(defaultName: String) {
        val data = getNameLiveData()
        if (data.value.isNullOrEmpty())
            data.value = defaultName
    }

    // region Teams Management

    fun performTeamsSearch(query: String?) {
        teamsRepository.performTeamsSearch(query.orEmpty().trim().toLowerCase(Locale.ROOT))
    }

    fun addTeamsFromMatches() {
        val existingTeamIds = teamsRepository.teamsLiveData.value.map { it.id }
        val matchesList = matchesRepository.matchesLiveData.value

        viewModelScope.launch(Dispatchers.Default) {
            val teamIds = HashSet<Int>(matchesList.size)

            matchesList.forEach {
                teamIds.add(it.redAlliance.firstTeam)
                teamIds.add(it.redAlliance.secondTeam)
                teamIds.add(it.blueAlliance.firstTeam)
                teamIds.add(it.blueAlliance.secondTeam)
            }

            val newTeamsList = teamIds.asSequence()
                .filter { it > 0 }
                .filterNot { existingTeamIds.contains(it) }
                .map { Team("", "", it, null) }
                .toList()

            teamsRepository.addTeams(tournamentKey, newTeamsList)
        }
    }

    fun updateTeam(team: Team) {
        val key = team.key

        if (key.isEmpty())
            teamsRepository.addTeam(tournamentKey, team)
        else
            teamsRepository.updateTeam(tournamentKey, team)
    }

    fun deleteTeam(teamKey: String) {
        teamsRepository.deleteTeam(tournamentKey, teamKey)
    }

    // endregion

    // region Match Management

    fun updateMatch(match: Match) {
        val key = match.key

        if (key.isEmpty())
            matchesRepository.addMatch(tournamentKey, match)
        else
            matchesRepository.updateMatch(tournamentKey, match)
    }

    fun deleteMatch(matchKey: String) {
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

    fun refreshAnalyticsData(showToast: Boolean = true) {
        val teams = teamsRepository.teamsLiveData.value
        val matches = matchesRepository.matchesLiveData.value

        if (matches.isEmpty()) {
            if (showToast)
                app.toast(R.string.opr_error_no_matches)
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val powerRankings = tournamentRepository.generateOprList(teams, matches)

            launch(Dispatchers.Main) {
                analyticsData.value = powerRankings

                if (powerRankings.isEmpty() && showToast)
                    app.toast(R.string.opr_error_data)
            }
        }
    }

    // region Spreadsheet

    fun exportToSpreadsheet(fileUri: Uri) {
        val teams = teamsRepository.teamsLiveData.value
        val matches = matchesRepository.matchesLiveData.value

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
    }

    fun importFromSpreadSheet(fileUri: Uri) {
        val currentTeams = teamsRepository.teamsLiveData.value
        val currentMatches = matchesRepository.matchesLiveData.value

        viewModelScope.launch(Dispatchers.IO) {
            app.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                val import = SpreadsheetImport(inputStream)
                val importedTeams = import.getTeams()
                val importedMatches = import.getMatches()

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
