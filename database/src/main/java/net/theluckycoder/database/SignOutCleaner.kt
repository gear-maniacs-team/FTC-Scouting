package net.theluckycoder.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.core.utils.UserTeamPreferences
import javax.inject.Inject

class SignOutCleaner @Inject constructor(
    private val database: AppDatabase,
    private val appPreferences: AppPreferences,
    private val userTeamPreferences: UserTeamPreferences
) {

    fun run() = GlobalScope.launch(Dispatchers.IO) {
        database.clearAllTables()

        appPreferences.setLoggedIn(false)

        userTeamPreferences.updateUserTeam(UserTeam())
    }
}
