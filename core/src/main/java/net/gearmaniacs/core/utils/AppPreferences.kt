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

    val seenIntro = preferences.getBoolean(SEEN_INTRO)

    val isLoggedIn = preferences.getBoolean(IS_LOGGED_IN, false)

    private companion object {
        private const val SEEN_INTRO = "key_intro_seen"
        private const val IS_LOGGED_IN = "is_logged_in"
    }
}
