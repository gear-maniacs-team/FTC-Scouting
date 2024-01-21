package net.gearmaniacs.tournament.ui.tabs

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.parcelize.Parcelize
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.database.model.match.Match
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@Parcelize
internal data class InfoTab(private val userTeam: UserTeam?) : BottomTab, Parcelable {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            0.toUShort(),
            stringResource(R.string.title_info),
            painterResource(R.drawable.ic_info_outline)
        )

    override val selectedIcon: Painter
        @Composable get() = painterResource(R.drawable.ic_info_filled)

    @Composable
    override fun Content() = Box(
        Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {

        if (userTeam.isNullOrEmpty()) {
            Text(
                stringResource(R.string.team_details_not_found),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp),
            )
        } else {
            val viewModel: TournamentViewModel = viewModel()

            val infoData by remember(userTeam) { viewModel.getInfoFlow(userTeam) }.collectAsState()

            if (infoData.isEmpty()) {
                Text(
                    stringResource(R.string.empty_tab_info),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            } else {
                Cards(infoData)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Cards(infoData: List<Match>) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(infoData, key = { it.key }) {
                ElevatedCard(
                    Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                        .padding(8.dp)
                ) {
                    InfoCard(it)
                }
            }
        }
    }

    @Composable
    private fun InfoCard(match: Match) = Column(Modifier.padding(12.dp)) {
        Text("Match #${match.id}", fontSize = 19.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(
                R.string.match_info_red_alliance,
                match.redAlliance.firstTeam,
                match.redAlliance.secondTeam
            ),
            fontSize = 16.sp
        )

        Text(
            stringResource(
                R.string.match_info_blue_alliance,
                match.blueAlliance.firstTeam,
                match.blueAlliance.secondTeam
            ),
            fontSize = 16.sp
        )

        val redScore = match.redAlliance.score
        val blueScore = match.blueAlliance.score

        if (redScore != 0 && blueScore != 0) {
            val winner = stringResource(
                when {
                    blueScore > redScore -> R.string.match_blue_won
                    blueScore < redScore -> R.string.match_red_won
                    else -> R.string.match_draw
                }
            )

            Text(
                stringResource(
                    R.string.match_basic_info,
                    redScore,
                    blueScore,
                    winner
                ),
                fontSize = 17.5.sp
            )
        }
    }
}