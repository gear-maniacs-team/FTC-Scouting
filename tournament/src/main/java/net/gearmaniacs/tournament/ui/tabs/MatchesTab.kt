package net.gearmaniacs.tournament.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.ExpandableItem
import net.gearmaniacs.tournament.ui.screen.EditMatchScreen
import net.gearmaniacs.tournament.utils.filterTeamsByQuery
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal object MatchesTab : BottomTab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            2.toUShort(),
            stringResource(R.string.title_matches),
            painterResource(R.drawable.ic_matches_outline)
        )

    override val selectedIcon: Painter
        @Composable get() = painterResource(R.drawable.ic_matches_filled)

    private var nextMatchId = 0

    @Composable
    override fun FloatingAction() {
        val nav = LocalNavigator.currentOrThrow

        FloatingActionButton(onClick = { nav.push(EditMatchScreen(nextMatchId)) }) {
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
        val matches by viewModel.matchesFlow.collectAsState(emptyList())
        val teams = viewModel.teamsFlow.collectAsState(emptyList())

        var teamQuery by rememberSaveable { mutableStateOf("") }
        var filteredMatches by remember { mutableStateOf(emptyList<Match>()) }

        LaunchedEffect(matches, teamQuery) {
            nextMatchId = if (matches.isNotEmpty()) matches.lastIndex else 0

            filteredMatches = if (teamQuery.isBlank()) {
                matches
            } else {
                withContext(Dispatchers.Default) {
                    val teamsList = teams.value.asSequence()
                        .filterTeamsByQuery(teamQuery)
                        .map { it.number }
                        .toList()

                    ensureActive()

                    matches.filter { match ->
                        teamsList.any { match.containsTeam(it) }
                    }
                }
            }
        }
        if (matches.isEmpty() && teamQuery.isEmpty()) {
            Text(stringResource(R.string.empty_tab_matches))
        } else {
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                item {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        value = teamQuery,
                        onValueChange = { teamQuery = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.action_search)) },
                        placeholder = { Text(stringResource(R.string.action_search_team)) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                        trailingIcon = {
                            if (teamQuery.isNotBlank()) {
                                IconButton(onClick = { teamQuery = "" }) {
                                    Icon(painterResource(R.drawable.ic_close), null)
                                }
                            }
                        },
                    )

                    Spacer(Modifier.height(16.dp))

                    Divider()
                }

                items(filteredMatches, key = { it.key }) { match ->
                    MatchItem(viewModel, match)

                    Divider()
                }
            }
        }
    }

    @Composable
    private fun MatchItem(viewModel: TournamentViewModel, match: Match) {
        val nav = LocalNavigator.current
        var showDialog by remember { mutableStateOf(false) }

        val redScore = match.redAlliance.score
        val blueScore = match.blueAlliance.score
        val winner = stringResource(
            when {
                blueScore > redScore -> R.string.match_blue_won
                blueScore < redScore -> R.string.match_red_won
                blueScore == 0 && redScore == 0 -> R.string.match_not_played
                else -> R.string.match_draw
            }
        )

        val winnerColor = colorResource(
            when {
                blueScore > redScore -> R.color.alliance_blue
                blueScore < redScore -> R.color.alliance_red
                else -> android.R.color.white
            }
        )

        val title = buildAnnotatedString {
            append("#${match.id}: ")
            withStyle(style = SpanStyle(winnerColor)) {
                append(winner)
            }
        }

        if (showDialog) {
            val message =
                if (Firebase.isLoggedIn) R.string.delete_match_desc else R.string.delete_match_desc_offline

            AlertDialog(
                onDismissRequest = { showDialog = false },
                icon = { Icon(painterResource(R.drawable.ic_matches_filled), null) },
                title = { Text(stringResource(R.string.delete_match)) },
                text = { Text(stringResource(message)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteMatch(match.key)
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
            title = title,
            subtitle = stringResource(
                R.string.match_score_details,
                match.redAlliance.score,
                match.blueAlliance.score
            ),
            description = stringResource(
                R.string.match_detailed_info, match.redAlliance.firstTeam,
                match.redAlliance.secondTeam,
                match.blueAlliance.firstTeam,
                match.blueAlliance.secondTeam,
            ),
            onEditAction = { nav?.push(EditMatchScreen(nextMatchId, match)) },
            onDeleteAction = { showDialog = true }
        )
    }
}
