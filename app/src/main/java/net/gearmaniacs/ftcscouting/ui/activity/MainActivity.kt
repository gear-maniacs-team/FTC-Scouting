package net.gearmaniacs.ftcscouting.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LocalNavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.NavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleOwner
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.ui.theme.AppTheme
import net.gearmaniacs.ftcscouting.ui.screen.MainScreen
import net.gearmaniacs.login.ui.screen.LoginScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val emptyLifecycleProvider = object : NavigatorScreenLifecycleProvider {
            @ExperimentalVoyagerApi
            override fun provide(screen: Screen): List<ScreenLifecycleOwner> = emptyList()
        }

        setContent {
            AppTheme {
                CompositionLocalProvider(
                    LocalNavigatorScreenLifecycleProvider provides emptyLifecycleProvider
                ) {
                    Navigator(
                        MainScreen,
                        onBackPressed = { it.uniqueScreenKey != MainScreen.key && it.key != LoginScreen.KEY }) {
                        FadeTransition(navigator = it)
                    }
                }
            }
        }
    }
}
