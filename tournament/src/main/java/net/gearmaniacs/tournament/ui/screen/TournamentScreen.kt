package net.gearmaniacs.tournament.ui.screen

import android.app.Activity
import android.net.Uri
import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import net.gearmaniacs.tournament.ui.dialog.DeleteTournamentDialog
import net.gearmaniacs.tournament.ui.dialog.NewTournamentDialog
import net.gearmaniacs.tournament.ui.tabs.BottomTab
import net.gearmaniacs.tournament.ui.tabs.InfoTab
import net.gearmaniacs.tournament.ui.tabs.LeaderboardTab
import net.gearmaniacs.tournament.ui.tabs.MatchesTab
import net.gearmaniacs.tournament.ui.tabs.TeamsTab
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@Parcelize
@OptIn(ExperimentalMaterial3Api::class)
internal data class TournamentScreen(private val userTeam: UserTeam?) : Screen, Parcelable {

    @Composable
    override fun Content() {
        val viewModel: TournamentViewModel = viewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val ctx = LocalContext.current as Activity
        val navigator = LocalNavigator.currentOrThrow

        TabNavigator(InfoTab(userTeam)) { tabNavigator ->
            Scaffold(modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    AnimatedVisibility(
                        visible = viewModel.showAppBar.value,
                        enter = slideInVertically(),
                        exit = slideOutVertically(),
                    ) {
                        val tournament by viewModel.tournamentFlow.collectAsState(null)

                        TopAppBar(title = {
                            Text(tournament?.name.orEmpty())
                        }, navigationIcon = {
                            IconButton(onClick = { ctx.finish() }) {
                                Icon(
                                    painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = null
                                )
                            }
                        }, actions = { TopBarActions() })
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
                }) { paddingValues ->

                CompositionLocalProvider(
                    LocalSnackbarHostState provides snackbarHostState,
                    LocalNavigator provides navigator
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        CurrentTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: BottomTab) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current == tab

    NavigationBarItem(selected = selected,
        onClick = { tabNavigator.current = tab },
        label = { Text(tab.options.title) },
        icon = {
            Icon(
                painter = if (selected) tab.selectedIcon else tab.options.icon!!,
                contentDescription = tab.options.title
            )
        })
}

@Composable
private fun TopBarActions() {
    val viewModel: TournamentViewModel = viewModel()
    val tournament by viewModel.tournamentFlow.collectAsState(null)
    val ctx = LocalContext.current as Activity

    var showEditNameSheet by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var addTeamsDialog by remember { mutableStateOf(false) }

    if (showEditNameSheet && tournament != null) {
        val (tournamentName, setTournamentName) = remember { mutableStateOf(tournament!!.name) }

        NewTournamentDialog(
            onDismiss = { showEditNameSheet = false },
            value = tournamentName,
            onValueChange = setTournamentName,
            confirmAction = {
                viewModel.updateTournamentName(tournamentName)
                showEditNameSheet = false
            },
            confirmButtonText = stringResource(R.string.action_update)
        )
    }

    if (showDeleteDialog) {
        DeleteTournamentDialog(onDismiss = { showDeleteDialog = false }, onDelete = {
            viewModel.deleteTournament()
            showDeleteDialog = false
            ctx.finish()
        })
    }

    if (addTeamsDialog) {
        AlertDialog(onDismissRequest = { addTeamsDialog = false },
            title = { Text(stringResource(R.string.add_teams_from_matches)) },
            text = { Text(stringResource(R.string.add_teams_from_matches_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addTeamsFromMatches()
                    addTeamsDialog = false
                }) {
                    Text(stringResource(R.string.action_add_teams))
                }
            },
            dismissButton = {
                TextButton(onClick = { addTeamsDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            })
    }

    IconButton(onClick = { showEditNameSheet = true }) {
        Icon(painterResource(R.drawable.ic_edit), null)
    }

    IconButton(onClick = { showOptions = true }) {
        Icon(painterResource(R.drawable.ic_more_vertical), null)
    }

    DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.action_delete_tournament)) },
            onClick = { showDeleteDialog = true },
            leadingIcon = { Icon(painterResource(R.drawable.ic_delete), null) })

        DropdownMenuItem(text = { Text(stringResource(R.string.action_add_teams)) },
            onClick = { addTeamsDialog = true },
            leadingIcon = { Icon(painterResource(R.drawable.ic_move_group), null) })

        Divider()

        CsvExport()
        CsvImport()
    }
}

@Composable
private fun CsvExport() {
    val viewModel: TournamentViewModel = viewModel()

    val tournament by viewModel.tournamentFlow.collectAsState(null)
    var expanded by remember { mutableStateOf(false) }

    val exportTeamsLauncher = rememberExportResult(viewModel::exportTeamsToCsv)
    val exportMatchesLauncher = rememberExportResult(viewModel::exportMatchesToCsv)
    val exportOprLauncher = rememberExportResult(viewModel::exportOprToCsv)

    DropdownMenuItem(
        text = { Text(stringResource(R.string.csv_export)) },
        onClick = { expanded = true },
    )

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            leadingIcon = { Icon(painterResource(R.drawable.ic_teams_filled), null) },
            text = { Text(stringResource(R.string.csv_export_teams)) },
            onClick = {
                expanded = false
                exportTeamsLauncher.launch(tournament?.name)
            },
        )

        DropdownMenuItem(
            leadingIcon = { Icon(painterResource(R.drawable.ic_matches_filled), null) },
            text = { Text(stringResource(R.string.csv_export_matches)) },
            onClick = {
                expanded = false
                exportMatchesLauncher.launch(tournament?.name)
            },
        )

        DropdownMenuItem(
            leadingIcon = { Icon(painterResource(R.drawable.ic_leaderboard_filled), null) },
            text = { Text(stringResource(R.string.csv_export_leaderboard)) },
            onClick = {
                expanded = false
                exportOprLauncher.launch(tournament?.name)
            },
        )
    }
}


@Composable
private fun CsvImport() {
    val viewModel: TournamentViewModel = viewModel()

    var expanded by remember { mutableStateOf(false) }

    val importTeamsLauncher = rememberImportResult(viewModel::importTeamsFromCsv)
    val importMatchesLauncher = rememberImportResult(viewModel::importMatchesFromCsv)

    DropdownMenuItem(
        text = { Text(stringResource(R.string.csv_import)) },
        onClick = { expanded = true },
    )

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            leadingIcon = { Icon(painterResource(R.drawable.ic_teams_filled), null) },
            text = { Text(stringResource(R.string.csv_import_teams)) },
            onClick = {
                expanded = false
                importTeamsLauncher.launch(arrayOf("text/*"))
            },
        )

        DropdownMenuItem(
            leadingIcon = { Icon(painterResource(R.drawable.ic_matches_filled), null) },
            text = { Text(stringResource(R.string.csv_import_matches)) },
            onClick = {
                expanded = false
                importMatchesLauncher.launch(arrayOf("text/*"))
            },
        )
    }
}

@Composable
private fun rememberExportResult(callback: (Uri) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) {
        if (it != null) callback(it)
    }

@Composable
private fun rememberImportResult(callback: (Uri) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) callback(it)
    }

