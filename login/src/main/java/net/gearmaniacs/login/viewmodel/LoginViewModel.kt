package net.gearmaniacs.login.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import net.gearmaniacs.core.utils.AppPreferences
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
) : ViewModel() {

    val seenIntroFlow = appPreferences.seenIntroFlow.distinctUntilChanged()

    suspend fun useOfflineAccount() {
        appPreferences.setHasOfflineAccount(true)
    }

}