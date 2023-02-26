package net.gearmaniacs.ftcscouting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.core.utils.UserTeamPreferences
import net.gearmaniacs.ftcscouting.repository.MainRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userDataPreferences: UserTeamPreferences,
    private val appPreferences: AppPreferences,
    private val repository: MainRepository
) : ViewModel() {

    val userTeamFlow = userDataPreferences.userTeamFlow
    val tournamentsFlow = repository.tournamentsFlow
    val hasOfflineAccountFlow = appPreferences.hasOfflineAccount.distinctUntilChanged()

    fun createNewTournament(tournamentName: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.createNewTournament(userTeamFlow.first(), tournamentName)
    }

    fun deleteTournament(tournament: Tournament) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTournament(tournament.key)
    }

    fun setLoggedIn() = viewModelScope.launch {
        appPreferences.setLoggedIn(true)
    }

    fun startListening() {
        viewModelScope.launch(Dispatchers.IO) { repository.startListener() }
    }
}
