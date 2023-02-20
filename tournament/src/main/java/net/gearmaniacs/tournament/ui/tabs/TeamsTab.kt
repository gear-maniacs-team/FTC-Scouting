package net.gearmaniacs.tournament.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.firebase.ktx.Firebase
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.enums.ColorMarker
import net.gearmaniacs.core.model.enums.PreferredZone
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.ExpandableItem
import net.gearmaniacs.tournament.ui.model.TeamSearchQuery
import net.gearmaniacs.tournament.ui.screen.EditTeamScreen
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
object TeamsTab : BottomTab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            1.toUShort(),
            stringResource(R.string.title_teams),
            painterResource(R.drawable.ic_teams_outline)
        )

    override val selectedIcon: Painter
        @Composable get() = painterResource(R.drawable.ic_teams_filled)

    @Composable
    override fun FloatingAction() {
        val nav = LocalNavigator.currentOrThrow

        FloatingActionButton(onClick = { nav.push(EditTeamScreen(null)) }) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null)
        }
    }

    @Composable
    override fun Content() = Box(
        Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        val viewModel: TournamentViewModel = viewModel()

        val listState = rememberLazyListState()
        val teams by viewModel.queriedTeamsFlow.collectAsState()
        val query = rememberSaveable { mutableStateOf(TeamSearchQuery()) }

        LaunchedEffect(query.value) {
            viewModel.queryTeams(query.value)
        }

        if (teams.isEmpty() && query.value.isEmpty()) {
            Text(
                stringResource(R.string.empty_tab_teams),
                modifier = Modifier.padding(32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(Modifier.fillMaxWidth(), state = listState) {
                item {
                    QueryOptions(query)

                    Divider()
                }

                items(teams, key = { it.key }) { team ->
                    TeamItem(viewModel, team)

                    Divider()
                }
            }
        }
    }

    @Composable
    private fun QueryOptions(queryState: MutableState<TeamSearchQuery>) =
        Column(Modifier.padding(horizontal = 16.dp)) {
            var query by queryState

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query.name,
                onValueChange = { queryState.value = query.copy(name = it) },
                label = {
                    Text(stringResource(R.string.action_search))
                },
                placeholder = {
                    Text(stringResource(R.string.action_search_team))
                },
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_search), null)
                },
                trailingIcon = {
                    if (query.name.isNotBlank()) {
                        IconButton(onClick = { query = query.copy(name = "") }) {
                            Icon(painterResource(R.drawable.ic_close), null)
                        }
                    }
                },
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start)
            ) {
                QueryChip(R.string.color_default, query.defaultMarker) {
                    query = query.copy(defaultMarker = !query.defaultMarker)
                }
                QueryChip(R.string.color_red, query.redMarker) {
                    query = query.copy(redMarker = !query.redMarker)
                }
                QueryChip(R.string.color_blue, query.blueMarker) {
                    query = query.copy(blueMarker = !query.blueMarker)
                }
                QueryChip(R.string.color_green, query.greenMarker) {
                    query = query.copy(greenMarker = !query.greenMarker)
                }
                QueryChip(R.string.color_yellow, query.yellowMarker) {
                    query = query.copy(yellowMarker = !query.yellowMarker)
                }
            }
        }

    @Composable
    private fun QueryChip(label: Int, value: Boolean, onClick: () -> Unit) {
        FilterChip(selected = value, onClick = onClick, label = { Text(stringResource(label)) })
    }

    @Composable
    private fun TeamItem(viewModel: TournamentViewModel, team: Team) {
        val nav = LocalNavigator.current
        var showDialog by remember { mutableStateOf(false) }
        val markerColor = colorResource(ColorMarker.getResColor(team.colorMarker))

        val preferredLocation = when (team.preferredZone) {
            PreferredZone.LEFT -> R.string.team_starting_zone_left
            PreferredZone.RIGHT -> R.string.team_starting_zone_right
            else -> R.string.none
        }

        val description = stringResource(
            R.string.team_description, team.autonomousScore(), team.controlledScore(),
            team.endGameScore(), stringResource(preferredLocation), team.notes.orEmpty()
        )

        if (showDialog) {
            val message =
                if (Firebase.isLoggedIn) R.string.delete_team_desc else R.string.delete_team_desc_offline

            AlertDialog(
                onDismissRequest = { showDialog = false },
                icon = { Icon(painterResource(R.drawable.ic_teams_filled), null) },
                title = { Text(stringResource(R.string.delete_team)) },
                text = { Text(stringResource(message)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteTeam(team.key)
                        showDialog = false
                    }) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }

        ExpandableItem(
            title = AnnotatedString(
                stringResource(
                    R.string.team_id_name, team.number, team.name.orEmpty()
                )
            ),
            subtitle = stringResource(R.string.team_predicted_score, team.score()),
            description = description,
            onEditAction = { nav?.push(EditTeamScreen(team)) },
            onDeleteAction = { showDialog = true },
            extraIcon = if (team.colorMarker != ColorMarker.DEFAULT) {
                {
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(markerColor)
                    )
                }
            } else null
        )
    }
}
