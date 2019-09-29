package net.gearmaniacs.ftcscouting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.gearmaniacs.core.firebase.FirebaseDatabaseCallback
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.gearmaniacs.ftcscouting.repository.MainRepository

class MainViewModel : ViewModel() {

    private val repository = MainRepository(viewModelScope,
        object : FirebaseDatabaseCallback<User> {
            override fun onSuccess(result: User) {
                currentUser = result
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        }
    )
    private var listening = false
    var currentUser: User? = null

    fun getTournamentsData() = repository.tournamentData

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
        repository.createNewTournament(currentUser, tournamentName)
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
