package net.gearmaniacs.ftcscouting.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.gearmaniacs.ftcscouting.repository.MainRepository

class MainViewModel : ViewModel() {

    private val repository = MainRepository()
    private var listening = false

    fun getUserLiveData(): LiveData<User> = repository.userLiveData

    fun getTournamentsLiveData() = repository.tournamentsLiveData

    fun startListening() {
        if (listening) return
        listening = true

        viewModelScope.launch(Dispatchers.IO) {
            repository.addListener()
        }
    }

    fun stopListening() {
        if (!listening) return

        repository.removeListener()

        listening = false
    }

    fun createNewTournament(tournamentName: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.createNewTournament(getUserLiveData().value, tournamentName)
    }

    fun deleteTournament(tournament: Tournament) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTournament(tournament.key)
    }

    override fun onCleared() {
        stopListening()
    }
}
