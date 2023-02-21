package net.gearmaniacs.tournament.ui.screen

import android.os.Parcelable
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.model.match.Alliance
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.NumberField
import net.gearmaniacs.tournament.utils.filterTeamsByQuery
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Parcelize
internal class EditMatchScreen(
    private val nextMatchId: Int,
    private val match: Match? = null,
) : Screen, Parcelable {

    @Composable
    override fun Content() = Column(Modifier.fillMaxSize()) {
        val viewModel: TournamentViewModel = viewModel()
        val navigator = LocalNavigator.currentOrThrow
        val ctx = LocalContext.current
        val scrollState = rememberScrollState()
        val teams = viewModel.teamsFlow.collectAsState(emptyList())

        var matchNumber by rememberSaveable {
            mutableStateOf((match?.id ?: nextMatchId).toString())
        }
        var redFirstTeam by rememberSaveable {
            mutableStateOf(match?.redAlliance?.firstTeam?.toString().orEmpty())
        }
        var redSecondTeam by rememberSaveable {
            mutableStateOf(match?.redAlliance?.secondTeam?.toString().orEmpty())
        }
        var redScore by rememberSaveable {
            mutableStateOf(match?.redAlliance?.score?.toString().orEmpty())
        }
        var blueFirstTeam by rememberSaveable {
            mutableStateOf(match?.blueAlliance?.firstTeam?.toString().orEmpty())
        }
        var blueSecondTeam by rememberSaveable {
            mutableStateOf(match?.blueAlliance?.secondTeam?.toString().orEmpty())
        }
        var blueScore by rememberSaveable {
            mutableStateOf(match?.blueAlliance?.score?.toString().orEmpty())
        }

        Column(
            Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .safeDrawingPadding()
        ) {
            NumberField(
                modifier = Modifier.fillMaxWidth(),
                value = matchNumber,
                onValueChange = { matchNumber = it },
                hint = stringResource(R.string.prompt_match_number),
                maxLength = 4,
            )

            Text(
                stringResource(R.string.red_alliance),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.alliance_red),
                modifier = Modifier.padding(top = 24.dp)
            )

            Row {
                TeamField(
                    modifier = Modifier.weight(1f),
                    value = redFirstTeam,
                    onValueChange = { redFirstTeam = it },
                    hint = stringResource(R.string.prompt_first_team),
                    teams = teams,
                )

                Spacer(Modifier.width(8.dp))

                TeamField(
                    modifier = Modifier.weight(1f),
                    value = redSecondTeam,
                    onValueChange = { redSecondTeam = it },
                    hint = stringResource(R.string.prompt_second_team),
                    teams = teams,
                )
            }

            NumberField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = redScore,
                onValueChange = { redScore = it },
                hint = stringResource(R.string.prompt_score),
                maxLength = 4,
            )

            Text(
                stringResource(R.string.blue_alliance),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.alliance_blue),
                modifier = Modifier.padding(top = 24.dp)
            )

            Row {
                TeamField(
                    modifier = Modifier.weight(1f),
                    value = blueFirstTeam,
                    onValueChange = { blueFirstTeam = it },
                    hint = stringResource(R.string.prompt_first_team),
                    teams = teams,
                )

                Spacer(Modifier.width(8.dp))

                TeamField(
                    modifier = Modifier.weight(1f),
                    value = blueSecondTeam,
                    onValueChange = { blueSecondTeam = it },
                    hint = stringResource(R.string.prompt_second_team),
                    teams = teams,
                )
            }

            NumberField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = blueScore,
                onValueChange = { blueScore = it },
                hint = stringResource(R.string.prompt_score),
                maxLength = 4,
            )
        }

        BottomAppBar(
            actions = {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(painterResource(R.drawable.ic_close), null)
                }
            },
            floatingActionButton = {
                FloatingAction {
                    try {
                        val redAlliance = Alliance(
                            firstTeam = redFirstTeam.toInt(),
                            secondTeam = redSecondTeam.toInt(),
                            score = redScore.toInt()
                        )

                        val blueAlliance = Alliance(
                            firstTeam = blueFirstTeam.toInt(),
                            secondTeam = blueSecondTeam.toInt(),
                            score = blueScore.toInt()
                        )

                        val parsedMatch = Match(
                            key = match?.key.orEmpty(),
                            tournamentKey = match?.tournamentKey ?: viewModel.tournamentKey,
                            id = matchNumber.toInt(),
                            redAlliance = redAlliance,
                            blueAlliance = blueAlliance
                        )

                        viewModel.updateMatch(parsedMatch)
                        navigator.pop()
                    } catch (_: NumberFormatException) {
                        Toast.makeText(ctx, "Invalid Number", Toast.LENGTH_SHORT).show()
                    }
                }
            })

    }

    @Composable
    private fun FloatingAction(onClick: () -> Unit) {
        val visibleState = remember { MutableTransitionState(false) }.apply { targetState = true }

        AnimatedVisibility(
            visibleState = visibleState, enter = scaleIn(), exit = scaleOut()
        ) {
            FloatingActionButton(
                onClick = onClick,
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(painterResource(R.drawable.ic_done), contentDescription = null)
            }
        }
    }

    @Composable
    private fun TeamField(
        modifier: Modifier,
        value: String,
        onValueChange: (String) -> Unit,
        hint: String,
        teams: State<List<Team>>
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            NumberField(
                modifier = modifier.menuAnchor(),
                value = value,
                onValueChange = {
                    onValueChange(it)
                    expanded = true
                },
                hint = hint,
                maxLength = 7,
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize(),
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = true,
                ),
                content = {
                    var options by remember { mutableStateOf(emptyList<Team>()) }

                    LaunchedEffect(teams.value, value) {
                        options = if (value.isBlank()) {
                            emptyList()
                        } else {
                            withContext(Dispatchers.Default) {
                                teams.value.asSequence().filterTeamsByQuery(value).take(5).toList()
                            }
                        }
                    }

                    options.forEach { team ->
                        DropdownMenuItem(
                            text = {
                                val name = if (team.name != null) " - " + team.name else ""
                                Text("${team.number}$name")
                            },
                            onClick = {
                                onValueChange(team.number.toString())
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            )
        }
    }
}