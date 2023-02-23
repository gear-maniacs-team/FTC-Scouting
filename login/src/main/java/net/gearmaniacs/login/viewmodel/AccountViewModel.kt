package net.gearmaniacs.login.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.extensions.app
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.login.repository.AccountRepository
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository,
    application: Application
) : AndroidViewModel(application) {

    val userTeamFlow = repository.userTeamFlow

    fun updateUserData(userTeam: UserTeam) = viewModelScope.launch(Dispatchers.IO) {
        val stringInt = repository.updateUserData(userTeam)

        withContext(Dispatchers.Main) {
            app.toast(stringInt)
        }
    }
}
