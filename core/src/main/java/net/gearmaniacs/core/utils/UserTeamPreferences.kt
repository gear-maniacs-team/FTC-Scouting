package net.gearmaniacs.core.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.gearmaniacs.core.model.UserTeam
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserTeamPreferences @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val userTeamDataStore = context.userDataStore

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
        private val Context.userDataStore by preferencesDataStore("user_team")

        private val USER_TEAM_NUMBER = intPreferencesKey("user_team_number")
        private val USER_TEAM_NAME = stringPreferencesKey("user_team_name")
    }
}
