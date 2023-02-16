package net.gearmaniacs.ftcscouting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.utils.UserTeamPreferences
import net.gearmaniacs.ftcscouting.repository.MainRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userDataPreferences: UserTeamPreferences,
    private val repository: MainRepository
) : ViewModel() {

    private var listening = false

    private val userDataLiveData = userDataPreferences.userTeamFlow.asLiveData()
    private val tournamentsLiveData = repository.tournamentsFlow.asLiveData()

    fun getUserTeamLiveData() = userDataLiveData

    fun getTournamentsLiveData() = tournamentsLiveData

    fun startListening() {
        if (listening) return
        listening = true

        viewModelScope.launch(Dispatchers.IO) { repository.addListener() }
    }

    fun stopListening() {
        if (!listening) return

        viewModelScope.launch(Dispatchers.IO) { repository.removeListener() }

        listening = false
    }

    fun createNewTournament(tournamentName: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.createNewTournament(userDataLiveData.value, tournamentName)
    }

    fun deleteTournament(tournament: Tournament) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTournament(tournament.key)
    }

    override fun onCleared() {
        GlobalScope.launch(Dispatchers.IO) { repository.clear() }
    }
}
