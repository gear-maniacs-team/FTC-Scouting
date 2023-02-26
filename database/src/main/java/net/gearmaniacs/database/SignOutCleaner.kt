package net.gearmaniacs.database

import android.content.Context
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.DelicateCoroutinesApi
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

    @OptIn(DelicateCoroutinesApi::class)
    fun run(ctx: Context) = GlobalScope.launch(Dispatchers.IO) {
        database.clearAllTables()

        appPreferences.setLoggedIn(false)

        userTeamPreferences.updateUserTeam(UserTeam())

        ProcessPhoenix.triggerRebirth(ctx)
    }
}
