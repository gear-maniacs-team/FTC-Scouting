package net.gearmaniacs.ftcscouting.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.view.WindowCompat
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.ui.theme.AppTheme
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.ftcscouting.ui.screen.MainScreen
import net.gearmaniacs.login.ui.screen.LoginScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme {
                Navigator(
                    MainScreen,
                    onBackPressed = { it.uniqueScreenKey != MainScreen.key && it.key != LoginScreen.KEY }) {
                    FadeTransition(navigator = it)
                }
            }
        }
    }
}
