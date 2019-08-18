package net.gearmaniacs.ftcscouting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.gearmaniacs.core.architecture.NonNullLiveData
import net.gearmaniacs.core.firebase.FirebaseDatabaseRepositoryCallback
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.gearmaniacs.ftcscouting.repository.MainRepository

class MainViewModel : ViewModel() {

    private val repository = MainRepository(viewModelScope)
    private var listening = false

    val tournamentData = NonNullLiveData(emptyList<Tournament>())
    var currentUser: User? = null

    init {
        repository.tournamentsCallback = object : FirebaseDatabaseRepositoryCallback<List<Tournament>> {
            override fun onSuccess(result: List<Tournament>) {
                tournamentData.value = result
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        }

        repository.userCallback = object : FirebaseDatabaseRepositoryCallback<User> {
            override fun onSuccess(result: User) {
                currentUser = result
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startListening() {
        if (listening) return

        repository.addListeners()

        listening = true
    }

    fun stopListening() {
        if (!listening) return

        repository.removeListeners()

        listening = false
    }

    fun createNewTournament(tournamentName: String) {
        currentUser?.let {
            repository.createNewTournament(it, tournamentName)
        }
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
