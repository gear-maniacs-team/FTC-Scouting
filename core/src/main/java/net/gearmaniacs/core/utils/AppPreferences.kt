package net.gearmaniacs.core.utils

import android.content.SharedPreferences
import com.tfcporciuncula.flow.FlowSharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class AppPreferences(
    sharedPreferences: SharedPreferences
) {
    private val preferences = FlowSharedPreferences(sharedPreferences)

    val seenIntroPref = preferences.getBoolean(KEY_SEEN_INTRO)

    val userDataNumberPref = preferences.getInt(KEY_TEAM_INFO_NUMBER, -1)
    val userDataNamePref = preferences.getString(KEY_TEAM_INFO_NAME, "")

    private companion object {
        private const val KEY_SEEN_INTRO = "key_intro_seen"

        private const val KEY_TEAM_INFO_NUMBER = "key_user_data_number"
        private const val KEY_TEAM_INFO_NAME = "key_user_data_name"
    }
}