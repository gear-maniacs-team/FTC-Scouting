package net.gearmaniacs.tournament.ui.screen

import android.os.Parcelable
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.model.enums.ColorMark
import net.gearmaniacs.core.model.enums.StartZone
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.core.ui.NumberField
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@OptIn(ExperimentalAnimationApi::class)
@Parcelize
internal class EditTeamScreen(private val team: Team?) : Screen, Parcelable {

    @Composable
    override fun Content() {
        val viewModel: TournamentViewModel = viewModel()
        val navigator = LocalNavigator.currentOrThrow
        val ctx = LocalContext.current
        val scrollState = rememberScrollState()

        var teamNumber by rememberSaveable {
            mutableStateOf(team?.number?.toString().orEmpty())
        }
        var teamName by rememberSaveable {
            mutableStateOf(team?.name.orEmpty())
        }
        var autoScore by rememberSaveable {
            mutableStateOf(team?.autonomousScore?.toString().orEmpty())
        }
        var teleOpScore by rememberSaveable {
            mutableStateOf(team?.teleOpScore?.toString().orEmpty())
        }
        var colorMark by rememberSaveable {
            mutableStateOf(team?.colorMark ?: ColorMark.DEFAULT)
        }
        var startZone by rememberSaveable {
            mutableStateOf(team?.startZone ?: StartZone.NONE)
        }
        var notes by rememberSaveable { mutableStateOf(team?.notes.orEmpty()) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomBar(onClick = {
                    try {
                        val parsedTeam = Team(
                            key = team?.key.orEmpty(),
                            tournamentKey = team?.tournamentKey ?: viewModel.tournamentKey,
                            number = teamNumber.toInt(),
                            name = teamName,
                            autonomousScore = autoScore.toInt(),
                            teleOpScore = teleOpScore.toInt(),
                            colorMark = colorMark,
                            startZone = startZone,
                            notes = notes,
                        )

                        viewModel.updateTeam(parsedTeam)
                        navigator.pop()
                    } catch (_: NumberFormatException) {
                        Toast.makeText(ctx, "Invalid Number", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
            ) {
                NumberField(
                    modifier = Modifier.fillMaxWidth(),
                    value = teamNumber,
                    onValueChange = { teamNumber = it },
                    hint = stringResource(R.string.prompt_team_number),
                    maxLength = 7,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text(stringResource(R.string.prompt_team_name)) },
                    singleLine = true,
                )

                Spacer(Modifier.height(32.dp))

                NumberField(
                    modifier = Modifier.fillMaxWidth(),
                    value = autoScore,
                    onValueChange = { autoScore = it },
                    hint = stringResource(R.string.prompt_autonomous_score),
                    maxLength = 3,
                )

                NumberField(
                    modifier = Modifier.fillMaxWidth(),
                    value = teleOpScore,
                    onValueChange = { teleOpScore = it },
                    hint = stringResource(R.string.prompt_driver_controlled_score),
                    maxLength = 3,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.team_notes)) },
                    singleLine = true,
                )
            }
        }
    }

    @Composable
    private fun BottomBar(onClick: () -> Unit) {
        val navigator = LocalNavigator.current

        BottomAppBar(
            actions = {
                IconButton(onClick = { navigator?.pop() }) {
                    Icon(painterResource(R.drawable.ic_close), null)
                }
            },
            floatingActionButton = {
                FloatingAction(onClick = onClick)
            }
        )
    }

    @Composable
    private fun FloatingAction(onClick: () -> Unit) {
        val visibleState =
            remember { MutableTransitionState(false) }.apply { targetState = true }

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
}