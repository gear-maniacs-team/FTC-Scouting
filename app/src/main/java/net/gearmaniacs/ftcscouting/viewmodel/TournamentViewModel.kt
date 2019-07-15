package net.gearmaniacs.ftcscouting.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.gearmaniacs.ftcscouting.model.Alliance
import net.gearmaniacs.ftcscouting.model.Match
import net.gearmaniacs.ftcscouting.model.Team
import net.gearmaniacs.ftcscouting.model.TeamPower
import net.gearmaniacs.ftcscouting.opr.PowerRanking
import net.gearmaniacs.ftcscouting.repository.TournamentRepository
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.InfoFragment
import net.gearmaniacs.ftcscouting.utils.architecture.MutexLiveData
import net.gearmaniacs.ftcscouting.utils.architecture.NonNullLiveData
import net.gearmaniacs.ftcscouting.utils.extensions.toast
import net.gearmaniacs.ftcscouting.utils.firebase.FirebaseDatabaseRepositoryCallback

class TournamentViewModel : ViewModel() {

    private var listening = false

    var tournamentKey = ""
        set(value) {
            stopListening()
            field = value
            startListening()
        }

    var fragmentTag = InfoFragment.TAG

    val nameData = MutableLiveData("")
    val teamsData = MutexLiveData(emptyList<Team>())
    val matchesData = MutexLiveData(emptyList<Match>())
    val analyticsData = NonNullLiveData(emptyList<TeamPower>())

    private val repository = TournamentRepository(viewModelScope, teamsData, matchesData)

    init {
        repository.nameCallback = object :
            FirebaseDatabaseRepositoryCallback<String?> {
            override fun onSuccess(result: String?) {
                nameData.value = result
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDefaultName(defaultName: String) {
        if (nameData.value.isNullOrEmpty())
            nameData.value = defaultName
    }

    // region Teams Management

    fun addTeams(teamIds: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTeams(tournamentKey, teamIds)
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

    fun calculateOpr(appContext: Context) {
        val teams = teamsData.value
        val matches = matchesData.value

        if (teams.isEmpty() || matches.isEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            val redAlliances = ArrayList<Alliance>(matches.size)
            val blueAlliances = ArrayList<Alliance>(matches.size)

            matches.forEach {
                redAlliances.add(it.redAlliance)
                blueAlliances.add(it.blueAlliance)
            }

            try {
                val powerRankings = PowerRanking(teams, redAlliances, blueAlliances).generatePowerRankings()

                analyticsData.postValue(powerRankings)
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    appContext.toast("Something went wrong. Please check the input data")
                }
            }
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
