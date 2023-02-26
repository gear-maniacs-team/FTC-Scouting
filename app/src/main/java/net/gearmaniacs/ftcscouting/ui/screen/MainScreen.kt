package net.gearmaniacs.ftcscouting.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.ui.activity.AboutActivity
import net.gearmaniacs.ftcscouting.viewmodel.MainViewModel
import net.gearmaniacs.login.ui.activity.AccountActivity
import net.gearmaniacs.login.ui.screen.LoginScreen
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.dialog.DeleteTournamentDialog
import net.gearmaniacs.tournament.ui.dialog.NewTournamentDialog

object MainScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val ctx = LocalContext.current
        val nav = LocalNavigator.currentOrThrow

        val viewModel: MainViewModel = viewModel()
        val userTeam by viewModel.userTeamFlow.collectAsState(null)
        val hasOfflineAccount by viewModel.hasOfflineAccountFlow.collectAsState(null)

        LaunchedEffect(Firebase.isLoggedIn, hasOfflineAccount) {
            if (Firebase.isLoggedIn) {
                viewModel.setLoggedIn()
                viewModel.startListening()
            } else if (hasOfflineAccount == false) {
                nav.push(LoginScreen())
            } else {
                viewModel.startListening()
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = {
                    Text(stringResource(R.string.title_tournaments))
                })
            },
            bottomBar = {
                BottomBar()
            }
        ) { paddingValues ->
            val tournaments by viewModel.tournamentsFlow.collectAsState(emptyList())

            LazyColumn(Modifier.padding(paddingValues)) {
                items(tournaments, key = { it.key }) {
                    TournamentItem(
                        it,
                        onClick = {
                            TournamentActivity.startActivity(ctx, userTeam, it)
                        },
                        onDelete = {
                            viewModel.deleteTournament(it)
                        }
                    )

                    Divider()
                }
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TournamentItem(
    tournament: Tournament,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    showDeleteDialog = true
                }
            )
            .padding(16.dp)
    ) {
        Text(tournament.name, fontSize = 20.sp)
    }

    if (showDeleteDialog) {
        DeleteTournamentDialog(
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar() {
    val viewModel: MainViewModel = viewModel()
    val userTeam by viewModel.userTeamFlow.collectAsState(null)

    var showCreateDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val bottomSheetState = rememberSheetState(true)

    BottomAppBar(
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(painterResource(R.drawable.ic_menu), contentDescription = "Menu")
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                icon = { Icon(painterResource(R.drawable.ic_add), null) },
                text = { Text(stringResource(R.string.action_create_tournament)) },
            )
        }
    )

    if (showCreateDialog) {
        val (tournamentName, setTournamentName) = remember { mutableStateOf("") }

        NewTournamentDialog(
            onDismiss = { showCreateDialog = false },
            value = tournamentName,
            onValueChange = setTournamentName,
            confirmAction = {
                viewModel.createNewTournament(tournamentName)
                showCreateDialog = false
            },
            confirmButtonText = stringResource(R.string.action_create),
        )
    }

    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMenu = false },
            sheetState = bottomSheetState,
        ) {
            MainMenuContent(userTeam)
        }
    }
}

@Composable
private fun MainMenuContent(userTeam: UserTeam?) = Column(
    Modifier
        .fillMaxWidth()
        .padding(16.dp)
) {
    val currentUser = Firebase.auth.currentUser
    val nav = LocalNavigator.currentOrThrow
    val ctx = LocalContext.current

    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)

    if (currentUser != null) {
        val defaultDisplayName = currentUser.displayName
        val teamName = userTeam?.teamName.orEmpty()
        val displayName: String =
            if (defaultDisplayName.isNullOrBlank()) teamName else defaultDisplayName
        Text(displayName, fontSize = 17.5.sp)

        currentUser.email?.let { email ->
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    } else {
        OutlinedButton(
            onClick = { nav.push(LoginScreen()) },
            modifier = buttonModifier
        ) {
            Text(stringResource(R.string.action_sign_in), fontSize = 16.sp)
        }
    }

    OutlinedButton(
        onClick = { AccountActivity.startActivity(ctx, userTeam) },
        modifier = buttonModifier,
    ) {
        Text(stringResource(R.string.account_and_team_details), fontSize = 16.sp)
    }

    OutlinedButton(
        onClick = { ctx.startActivity<AboutActivity>() },
        modifier = buttonModifier
    ) {
        Text(stringResource(R.string.title_about), fontSize = 16.sp)
    }
}