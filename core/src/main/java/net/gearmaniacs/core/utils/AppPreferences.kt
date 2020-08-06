package net.gearmaniacs.core.utils

import android.content.SharedPreferences
import com.tfcporciuncula.flow.FlowSharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AppPreferences @Inject constructor(
    sharedPreferences: SharedPreferences
) {
    private val preferences = FlowSharedPreferences(sharedPreferences)

    val seenIntro = preferences.getBoolean(KEY_SEEN_INTRO)

    val firstStartUp = preferences.getBoolean(KEY_FIRST_STARTUP, true)

    val hasOfflineAccount = preferences.getBoolean(KEY_HAS_OFFLINE_ACCOUNT, true)

    val userDataNumber = preferences.getInt(KEY_TEAM_INFO_NUMBER, -1)

    val userDataName = preferences.getString(KEY_TEAM_INFO_NAME, "")

    private companion object {
        private const val KEY_SEEN_INTRO = "key_intro_seen"
        private const val KEY_FIRST_STARTUP = "key_first_start_up"
        private const val KEY_HAS_OFFLINE_ACCOUNT = "key_has_offline_account"
        private const val KEY_TEAM_INFO_NUMBER = "key_user_data_number"
        private const val KEY_TEAM_INFO_NAME = "key_user_data_name"
    }
}
