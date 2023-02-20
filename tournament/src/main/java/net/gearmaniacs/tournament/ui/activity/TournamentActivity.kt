package net.gearmaniacs.tournament.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideOrientation
import cafe.adriel.voyager.transitions.SlideTransition
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.core.ui.theme.AppTheme
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.fragment.NewTournamentDialog
import net.gearmaniacs.tournament.ui.screen.TournamentScreen
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
class TournamentActivity : AppCompatActivity() {

    private val viewModel by viewModels<TournamentViewModel>()

    private var teamsList = emptyList<Team>()
    private var matchesList = emptyList<Match>()

    private val exportSpreadsheetLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it?.data?.data?.let { documentUri ->
                viewModel.exportToSpreadsheet(documentUri, teamsList, matchesList)
            }
        }
    private val importSpreadsheetLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it?.data?.data?.let { documentUri ->
                viewModel.importFromSpreadSheet(documentUri, teamsList, matchesList)
            }
        }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make sure the data from the intent is not null
        viewModel.tournamentKey = intent.getStringExtra(ARG_TOURNAMENT_KEY)
            ?: throw IllegalArgumentException("Missing $ARG_TOURNAMENT_KEY")
        viewModel.startListening()

        setContent {
            AppTheme {
                val user = intent.getParcelableExtra<UserTeam>(ARG_USER)

                Navigator(TournamentScreen(user)) {
                    SlideTransition(it, orientation = SlideOrientation.Vertical)
                }
            }
        }


        lifecycleScope.launch {
            viewModel.teamsFlow.collectLatest { teamsList = it }
        }

        lifecycleScope.launch {
            viewModel.matchesFlow.collectLatest { matchesList = it }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_opr_info -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.opr_info)
                    .setMessage(R.string.opr_info_desc)
                    .setIcon(R.drawable.ic_info_outline)
                    .setNeutralButton(android.R.string.ok, null)
                    .show()
            }
            R.id.action_tournament_edit -> changeTournamentName()
            R.id.action_tournament_delete -> deleteTournament()
            R.id.action_add_teams -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.add_teams_from_matches)
                    .setMessage(R.string.add_teams_from_matches_desc)
                    .setPositiveButton(R.string.action_add_teams) { _, _ ->
                        viewModel.addTeamsFromMatches(teamsList, matchesList)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            R.id.action_export -> {
                val intent = getSpreadsheetIntent(Intent.ACTION_CREATE_DOCUMENT)
                intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                viewModel.getCurrentTournamentLiveData().value?.name?.let {
//                    intent.putExtra(Intent.EXTRA_TITLE, it)
//                }

                exportSpreadsheetLauncher.launch(intent)
            }
            R.id.action_import -> {
                val intent = getSpreadsheetIntent(Intent.ACTION_OPEN_DOCUMENT)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                importSpreadsheetLauncher.launch(intent)
            }
        }
        return true
    }

    private fun changeTournamentName() {
        val dialogFragment = NewTournamentDialog()
        dialogFragment.actionButtonStringRes = R.string.action_update
//        dialogFragment.defaultName = viewModel.getCurrentTournamentLiveData().value?.name

        dialogFragment.actionButtonListener = { newName ->
            viewModel.updateTournamentName(newName)
        }

        dialogFragment.show(supportFragmentManager, dialogFragment.tag)
    }

    private fun deleteTournament() {
        val message =
            if (Firebase.isLoggedIn) R.string.delete_tournament_desc else R.string.delete_tournament_desc_offline

        AlertDialog.Builder(this)
            .setTitle(R.string.delete_tournament)
            .setMessage(message)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteTournament()
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun getSpreadsheetIntent(action: String) =
        Intent(action).apply {
            type = "application/vnd.ms-excel"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

    companion object {
        const val ARG_TOURNAMENT_KEY = "tournament_key"
        const val ARG_USER = "user"

        fun startActivity(context: Context, userTeam: UserTeam, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java).apply {
                putExtra(ARG_USER, userTeam)
                putExtra(ARG_TOURNAMENT_KEY, tournament.key)
            }

            context.startActivity(intent)
        }
    }
}
