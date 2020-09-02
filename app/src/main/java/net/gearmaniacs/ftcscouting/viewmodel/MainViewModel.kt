package net.gearmaniacs.ftcscouting.viewmodel

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.utils.UserDataPreferences
import net.gearmaniacs.ftcscouting.repository.MainRepository
import net.theluckycoder.database.di.DatabaseModule

class MainViewModel @ViewModelInject constructor(
    userDataPreferences: UserDataPreferences,
    private val repository: MainRepository
) : ViewModel() {

    private var listening = false

    private val userDataLiveData = userDataPreferences.userTeamFlow.asLiveData()
    private val tournamentsLiveData = repository.tournamentsFlow.asLiveData()

    fun getUserLiveData() = userDataLiveData

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
        repository.createNewTournament(getUserLiveData().value, tournamentName)
    }

    fun deleteTournament(tournament: Tournament) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTournament(tournament.key)
    }

    fun signOut(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        DatabaseModule.providesAppDatabase(context.applicationContext)
            .clearAllTables()
    }

    override fun onCleared() {
        GlobalScope.launch(Dispatchers.IO) { repository.clear() }
    }
}
