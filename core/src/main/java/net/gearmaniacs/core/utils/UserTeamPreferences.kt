package net.gearmaniacs.core.utils

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.gearmaniacs.core.model.UserTeam
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class UserTeamPreferences @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val userTeamDataStore = context.createDataStore(name = DATA_STORE_USER_NAME)

    val userTeamFlow: Flow<UserTeam> = userTeamDataStore.data
        .map { preferences ->
            val number = preferences[USER_TEAM_NUMBER] ?: -1
            val name = preferences[USER_TEAM_NAME].orEmpty()

            UserTeam(number, name)
        }

    suspend fun updateUserTeam(userTeam: UserTeam) = userTeamDataStore.edit { preferences ->
        preferences[USER_TEAM_NUMBER] = userTeam.id
        preferences[USER_TEAM_NAME] = userTeam.teamName
    }

    private companion object {
        private const val DATA_STORE_USER_NAME = "user_team"
        private val USER_TEAM_NUMBER = preferencesKey<Int>("user_team_number")
        private val USER_TEAM_NAME = preferencesKey<String>("user_team_name")
    }
}
