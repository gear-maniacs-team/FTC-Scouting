package net.gearmaniacs.tournament.ui.screen

import android.app.Activity
import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.ui.LocalSnackbarHostState
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.tabs.BottomTab
import net.gearmaniacs.tournament.ui.tabs.InfoTab
import net.gearmaniacs.tournament.ui.tabs.LeaderboardTab
import net.gearmaniacs.tournament.ui.tabs.MatchesTab
import net.gearmaniacs.tournament.ui.tabs.TeamsTab
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@Parcelize
@OptIn(ExperimentalMaterial3Api::class)
data class TournamentScreen(private val userTeam: UserTeam?) : Screen, Parcelable {

    @Composable
    override fun Content() {
        val viewModel: TournamentViewModel = viewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val ctx = LocalContext.current as Activity
        val navigator = LocalNavigator.currentOrThrow

        TabNavigator(InfoTab(userTeam)) { tabNavigator ->
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    AnimatedVisibility(
                        visible = viewModel.showAppBar.value,
                        enter = slideInVertically(),
                        exit = slideOutVertically(),
                    ) {
                        val tournament by viewModel.tournamentFlow.collectAsState(null)

                        TopAppBar(
                            title = {
                                Text(tournament?.name.orEmpty())
                            },
                            navigationIcon = {
                                IconButton(onClick = { ctx.finish() }) {
                                    Icon(
                                        painterResource(R.drawable.ic_arrow_back),
                                        contentDescription = null
                                    )
                                }
                            },
                            actions = {
                                // TODO Add back all actions
                            }
                        )
                    }
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TabNavigationItem(remember(userTeam) { InfoTab(userTeam) })
                        TabNavigationItem(TeamsTab)
                        TabNavigationItem(MatchesTab)
                        TabNavigationItem(LeaderboardTab)
                    }
                },
                floatingActionButton = {
                    val tab = (tabNavigator.current as? BottomTab)

                    CompositionLocalProvider(LocalNavigator provides navigator) {
                        tab?.FloatingAction()
                    }
                }
            ) { paddingValues ->

                CompositionLocalProvider(
                    LocalSnackbarHostState provides snackbarHostState,
                    LocalNavigator provides navigator
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
//                BottomSheetNavigator {
                        CurrentTab()
//                }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(tab: BottomTab) {
        val tabNavigator = LocalTabNavigator.current
        val selected = tabNavigator.current == tab

        NavigationBarItem(
            selected = selected,
            onClick = { tabNavigator.current = tab },
            label = { Text(tab.options.title) },
            icon = {
                Icon(
                    painter = if (selected) tab.selectedIcon else tab.options.icon!!,
                    contentDescription = tab.options.title
                )
            }
        )
    }
}