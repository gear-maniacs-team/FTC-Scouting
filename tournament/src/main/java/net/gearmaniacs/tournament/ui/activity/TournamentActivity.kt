package net.gearmaniacs.tournament.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LocalNavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.NavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleOwner
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideOrientation
import cafe.adriel.voyager.transitions.SlideTransition
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.parcelable
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.ui.theme.AppTheme
import net.gearmaniacs.database.model.Tournament
import net.gearmaniacs.tournament.ui.screen.TournamentScreen
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
class TournamentActivity : ComponentActivity() {

    private val viewModel by viewModels<TournamentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make sure the data from the intent is not null
        viewModel.tournamentKey = intent.getStringExtra(ARG_TOURNAMENT_KEY)
            ?: throw IllegalArgumentException("Missing $ARG_TOURNAMENT_KEY")
        viewModel.startListening()

        val emptyLifecycleProvider = object : NavigatorScreenLifecycleProvider {
            @ExperimentalVoyagerApi
            override fun provide(screen: Screen): List<ScreenLifecycleOwner> = emptyList()
        }

        setContent {
            CompositionLocalProvider(
                LocalNavigatorScreenLifecycleProvider provides emptyLifecycleProvider
            ) {
                AppTheme {
                    val user = intent.parcelable<UserTeam>(ARG_USER)

                    Navigator(TournamentScreen(user)) {
                        SlideTransition(it, orientation = SlideOrientation.Vertical)
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_TOURNAMENT_KEY = "tournament_key"
        const val ARG_USER = "user"

        fun startActivity(context: Context, userTeam: UserTeam?, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java).apply {
                putExtra(ARG_USER, userTeam)
                putExtra(ARG_TOURNAMENT_KEY, tournament.key)
            }

            context.startActivity(intent)
        }
    }
}
