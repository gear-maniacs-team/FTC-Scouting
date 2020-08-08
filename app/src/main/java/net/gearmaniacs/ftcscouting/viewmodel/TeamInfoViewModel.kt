package net.gearmaniacs.ftcscouting.viewmodel

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.extensions.app
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.ftcscouting.repository.TeamInfoRepository

class TeamInfoViewModel @ViewModelInject constructor(
    private val repository: TeamInfoRepository,
    application: Application
) : AndroidViewModel(application) {

    fun updateUserData(userData: UserData) = viewModelScope.launch(Dispatchers.IO) {
        val stringInt = repository.updateUserData(userData)

        withContext(Dispatchers.Main) {
            app.toast(stringInt)
        }
    }
}
