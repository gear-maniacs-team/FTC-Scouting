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

        viewModelScope.launch(Dispatchers.Default) {
            repository.addListener()
        }
    }

    fun stopListening() {
        if (!listening) return

        repository.removeListener()

        listening = false
    }

    fun createNewTournament(tournamentName: String) {
        repository.createNewTournament(getUserLiveData().value, tournamentName)
    }

    fun deleteTournament(tournament: Tournament) {
        tournament.key?.let {
            repository.deleteTournament(it)
        }
    }

    override fun onCleared() {
        stopListening()
    }
}
