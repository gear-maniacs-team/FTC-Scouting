package net.gearmaniacs.tournament.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.tab.TabOptions
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel
import kotlin.random.Random

internal object LeaderboardTab : BottomTab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            3.toUShort(),
            stringResource(R.string.title_leaderboard),
            painterResource(R.drawable.ic_leaderboard_outline)
        )

    override val selectedIcon: Painter
        @Composable get() = painterResource(R.drawable.ic_leaderboard_filled)

    @Composable
    override fun Content() = Box(
        Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        val viewModel: TournamentViewModel = viewModel()

        val teams = viewModel.teamsFlow.collectAsState(emptyList())
        val matches = viewModel.matchesFlow.collectAsState(emptyList())
        var errorMessage by remember { mutableStateOf("") }

        LaunchedEffect(teams.value, matches.value) {
            errorMessage = viewModel.refreshLeaderboardData(teams.value, matches.value)
        }

        val rankedTeams by viewModel.leaderBoardFlow.collectAsState(emptyList())

        when {
            errorMessage.isNotEmpty() -> {
                Text(
                    errorMessage,
                    modifier = Modifier.padding(32.dp),
                    textAlign = TextAlign.Center
                )
            }
            rankedTeams.isEmpty() -> {
                Text(stringResource(R.string.empty_tab_leaderboard))
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(rankedTeams, key = { _, team -> team.number }) { index, team ->
                        LeaderboardItem(index, team)

                        Divider()
                    }
                }
            }
        }
    }

    @Composable
    private fun LeaderboardItem(index: Int, team: RankedTeam) {
        val color = remember(team.number) {
            PLACEMENT_BACKGROUND_COLORS.random(Random(team.number))
        }

        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorResource(color))
            ) {
                Text(
                    (index + 1).toString(),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Black
                )
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Text("${team.number} - ${team.name}", fontSize = 19.sp)
                Text("${team.score} Points", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val PLACEMENT_BACKGROUND_COLORS = intArrayOf(
    R.color.leaderboard_red,
    R.color.leaderboard_blue,
    R.color.leaderboard_green,
    R.color.leaderboard_yellow,
    R.color.leaderboard_light_blue,
    R.color.leaderboard_orange,
    android.R.color.white,
)
