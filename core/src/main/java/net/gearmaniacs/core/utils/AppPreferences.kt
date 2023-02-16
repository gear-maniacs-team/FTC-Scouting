package net.gearmaniacs.core.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val settingsDataStore = context.appDataStore

    val seenIntroFlow: Flow<Boolean> =
        settingsDataStore.data.map { it[SEEN_INTRO] ?: false }.distinctUntilChanged()

    suspend fun seenIntro() = seenIntroFlow.first()

    suspend fun setSeenIntro(value: Boolean) = settingsDataStore.edit { preferences ->
        preferences[SEEN_INTRO] = value
    }

    val isLoggedInFlow: Flow<Boolean> =
        settingsDataStore.data.map { it[IS_LOGGED_IN] ?: false }.distinctUntilChanged()

    suspend fun isLoggedIn() = isLoggedInFlow.first()

    suspend fun setLoggedIn(value: Boolean) = settingsDataStore.edit { preferences ->
        preferences[IS_LOGGED_IN] = value
    }

    val hasOfflineAccount: Flow<Boolean> =
        settingsDataStore.data.map { it[HAS_OFFLINE_ACCOUNT] ?: false }.distinctUntilChanged()

    suspend fun hasOfflineAccount() = hasOfflineAccount.first()

    suspend fun setHasOfflineAccount(value: Boolean) = settingsDataStore.edit { preferences ->
        preferences[HAS_OFFLINE_ACCOUNT] = value
    }

    private companion object {
        private val Context.appDataStore by preferencesDataStore("settings")

        private val SEEN_INTRO = booleanPreferencesKey("key_intro_seen")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val HAS_OFFLINE_ACCOUNT = booleanPreferencesKey("has_offline_account")
    }
}
