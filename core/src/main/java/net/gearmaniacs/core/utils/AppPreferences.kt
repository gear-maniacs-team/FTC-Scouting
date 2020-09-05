package net.gearmaniacs.core.utils

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val settingsDataStore = context.createDataStore(
        name = "settings",
        migrations = listOf(
            SharedPreferencesMigration(context, context.packageName + "_preferences")
        )
    )

    val seenIntroFlow: Flow<Boolean> =
        settingsDataStore.data.map { it[SEEN_INTRO] ?: false }

    suspend fun seenIntro() = seenIntroFlow.first()

    suspend fun setSeenIntro(value: Boolean) = settingsDataStore.edit { preferences ->
        preferences[SEEN_INTRO] = value
    }

    val isLoggedInFlow: Flow<Boolean> =
        settingsDataStore.data.map { it[IS_LOGGED_IN] ?: false }

    suspend fun isLoggedIn() = isLoggedInFlow.first()

    suspend fun setLoggedIn(value: Boolean) = settingsDataStore.edit { preferences ->
        preferences[IS_LOGGED_IN] = value
    }

    private companion object {
        private val SEEN_INTRO = preferencesKey<Boolean>("key_intro_seen")
        private val IS_LOGGED_IN = preferencesKey<Boolean>("is_logged_in")
    }
}
