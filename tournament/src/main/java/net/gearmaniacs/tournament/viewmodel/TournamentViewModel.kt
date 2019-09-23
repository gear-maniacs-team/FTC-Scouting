package net.gearmaniacs.tournament.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseDatabaseRepositoryCallback
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.repository.MatchesRepository
import net.gearmaniacs.tournament.repository.TeamsRepository
import net.gearmaniacs.tournament.repository.TournamentRepository
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetExport
import net.gearmaniacs.tournament.spreadsheet.SpreadsheetImport
import java.io.File
import java.util.Locale

class TournamentViewModel : ViewModel() {

    private val currentUserReference by lazy {
        FirebaseDatabase.getInstance()
            .getReference(DatabasePaths.KEY_SKYSTONE)
            .child(DatabasePaths.KEY_USERS)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val repository = TournamentRepository(currentUserReference)
    private val teamsRepository = TeamsRepository(currentUserReference)
    private val matchesRepository = MatchesRepository(currentUserReference)
    private var listening = false

    var tournamentKey = ""
        set(value) {
            stopListening()
            field = value
            startListening()
        }

    val nameData = MutableLiveData("")
    val analyticsData = NonNullLiveData(emptyList<TeamPower>())

    init {
        repository.nameChangeCallback = object : FirebaseDatabaseRepositoryCallback<String?> {
            override fun onSuccess(result: String?) {
                nameData.value = result
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTeamsLiveData(): NonNullLiveData<List<Team>> = teamsRepository.queriedLiveData

    fun getMatchesLiveData(): NonNullLiveData<List<Match>> = matchesRepository.liveData

    fun setDefaultName(defaultName: String) {
        if (nameData.value.isNullOrEmpty())
            nameData.value = defaultName
    }

    // region Teams Management

    fun performTeamsSearch(query: String?) {
        runBlocking {
            teamsRepository.performTeamsSearch(query.orEmpty().trim().toLowerCase(Locale.ROOT))
        }
    }

    fun addTeamsFromMatches() {
        val existingTeamIds = teamsRepository.liveData.value.map { it.id }
        val matchesList = matchesRepository.liveData.value

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

            teamsRepository.addTeams(tournamentKey, newTeamsList)
        }
    }

    fun updateTeam(team: Team) {
        val key = team.key

        if (key == null)
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

        if (key == null)
            matchesRepository.addMatch(tournamentKey, match)
        else
            matchesRepository.updateMatch(tournamentKey, match)
    }

    fun deleteMatch(matchKey: String) {
        matchesRepository.deleteMatch(tournamentKey, matchKey)
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
        val teams = teamsRepository.liveData.value
        val matches = matchesRepository.liveData.value

        if (matches.isEmpty()) {
            appContext.toast(R.string.opr_error_no_matches)
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val powerRankings = repository.generateOprList(teams, matches)

            launch(Dispatchers.Main) {
                analyticsData.value = powerRankings

                if (powerRankings.isEmpty())
                    appContext.toast(R.string.opr_error_data)
            }
        }
    }

    fun exportToSpreadsheet(appContext: Context, folderDestination: File) {
        val teams = teamsRepository.liveData.value
        val matches = matchesRepository.liveData.value

        viewModelScope.launch(Dispatchers.IO) {
            val powerRankings = repository.generateOprList(teams, matches)

            try {
                val export = SpreadsheetExport()
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
        val currentTeams = teamsRepository.liveData.value
        val currentMatches = matchesRepository.liveData.value

        viewModelScope.launch(Dispatchers.IO) {
            val import = SpreadsheetImport(file)
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

    fun startListening() {
        if (listening) return

        repository.addListeners(tournamentKey)
        teamsRepository.addListeners(tournamentKey)
        matchesRepository.addListeners(tournamentKey)

        listening = true
    }

    fun stopListening() {
        if (!listening) return

        repository.removeListeners(tournamentKey)
        teamsRepository.removeListeners(tournamentKey)
        matchesRepository.removeListeners(tournamentKey)

        listening = false
    }

    override fun onCleared() {
        stopListening()
    }
}
