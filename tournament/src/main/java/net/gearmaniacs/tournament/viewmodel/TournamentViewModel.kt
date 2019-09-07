package net.gearmaniacs.tournament.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.firebase.FirebaseDatabaseRepositoryCallback
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.repository.TournamentRepository
import net.gearmaniacs.tournament.spreadsheet.ExportToSpreadsheet
import net.gearmaniacs.tournament.spreadsheet.ImportFromSpreadsheet
import java.io.File
import java.util.Locale

class TournamentViewModel : ViewModel() {

    private var listening = false

    var tournamentKey = ""
        set(value) {
            stopListening()
            field = value
            startListening()
        }

    val nameData = MutableLiveData("")
    val analyticsData = NonNullLiveData(emptyList<TeamPower>())

    private val repository = TournamentRepository(viewModelScope)

    init {
        repository.nameCallback = object : FirebaseDatabaseRepositoryCallback<String?> {
            override fun onSuccess(result: String?) {
                nameData.value = result
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTeamsLiveData(): NonNullLiveData<List<Team>> = repository.filteredTeamsData

    fun getMatchesLiveData(): NonNullLiveData<List<Match>> = repository.matchesData

    fun setDefaultName(defaultName: String) {
        if (nameData.value.isNullOrEmpty())
            nameData.value = defaultName
    }

    // region Teams Management

    fun performTeamsSearch(query: String?) {
        repository.performTeamsSearch(query.orEmpty().trim().toLowerCase(Locale.ROOT))
    }

    fun addTeamsFromMatches() {
        val existingTeamIds = repository.teamsData.value.map { it.id }
        val matchesList = repository.matchesData.value

        viewModelScope.launch(Dispatchers.Default) {
            val teamIds = HashSet<Int>(matchesList.size)

            matchesList.forEach {
                teamIds.add(it.redAlliance.firstTeam)
                teamIds.add(it.redAlliance.secondTeam)
                teamIds.add(it.blueAlliance.firstTeam)
                teamIds.add(it.blueAlliance.secondTeam)
            }

            val newTeamsList = teamIds.asSequence()
                .filterNot { it > 0 }
                .filterNot { existingTeamIds.contains(it) }
                .map { Team(it, null) }
                .toList()

            repository.addTeams(tournamentKey, newTeamsList)
        }
    }

    fun updateTeam(team: Team) {
        val key = team.key

        if (key == null)
            repository.addTeam(tournamentKey, team)
        else
            repository.updatedTeam(tournamentKey, team)
    }

    fun deleteTeam(teamKey: String) {
        repository.deleteTeam(tournamentKey, teamKey)
    }

    // endregion

    // region Match Management

    fun updateMatch(match: Match) {
        val key = match.key

        if (key == null)
            repository.addMatch(tournamentKey, match)
        else
            repository.updatedMatch(tournamentKey, match)
    }

    fun deleteMatch(matchKey: String) {
        repository.deleteMatch(tournamentKey, matchKey)
    }

    // endregion

    // region Tournament Management

    fun updateTournamentName(newName: String) {
        if (newName.isNotBlank())
            repository.updateTournamentName(tournamentKey, newName)
    }

    fun deleteTournament() {
        repository.deleteTournament(tournamentKey)
    }

    // endregion

    fun refreshAnalyticsData(appContext: Context) {
        val matches = repository.matchesData.value

        if (matches.isEmpty()) {
            appContext.toast(R.string.opr_error_no_matches)
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val powerRankings = repository.generateOprList()

            launch(Dispatchers.Main) {
                analyticsData.value = powerRankings

                if (powerRankings.isEmpty())
                    appContext.toast(R.string.opr_error_data)
            }
        }
    }

    fun exportToSpreadsheet(appContext: Context, folderDestination: File) {
        val teams = repository.teamsData.value
        val matches = repository.matchesData.value

        viewModelScope.launch(Dispatchers.IO) {
            val powerRankings = repository.generateOprList()

            try {
                val export = ExportToSpreadsheet()
                export.export(teams, matches, powerRankings)

                val name = nameData.value
                val file = File(folderDestination, "$name.xls")
                export.saveToFile(file)

                launch(Dispatchers.Main) {
                    appContext.toast(
                        appContext.getString(R.string.spreadsheet_saved_successfully, name)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    appContext.toast(R.string.spreadsheet_error)
                }
            }
        }
    }

    fun importFromSpreadSheet(file: File) {
        val currentTeams = repository.teamsData.value
        val currentMatches = repository.matchesData.value

        viewModelScope.launch(Dispatchers.IO) {
            val import = ImportFromSpreadsheet(file)
            val importTeams = import.getTeams()
            val importMatches = import.getMatches()

            repository.addTeams(
                tournamentKey,
                importTeams.filterNot { currentTeams.contains(it) }
            )
            repository.addMatches(
                tournamentKey,
                importMatches.filterNot { currentMatches.contains(it) }
            )
        }
    }

    fun startListening() {
        if (listening) return

        repository.addListeners(tournamentKey)

        listening = true
    }

    fun stopListening() {
        if (!listening) return

        repository.removeListeners(tournamentKey)

        listening = false
    }

    override fun onCleared() {
        stopListening()
    }
}
