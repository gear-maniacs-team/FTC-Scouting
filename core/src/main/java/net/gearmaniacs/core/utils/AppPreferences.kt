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

    val firstStartUp = preferences.getBoolean(FIRST_STARTUP, true)

    private companion object {
        private const val SEEN_INTRO = "key_intro_seen"
        private const val FIRST_STARTUP = "key_first_start_up"
    }
}
