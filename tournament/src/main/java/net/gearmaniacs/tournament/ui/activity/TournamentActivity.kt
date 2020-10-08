package net.gearmaniacs.tournament.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.commit
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.TournamentActivityBinding
import net.gearmaniacs.tournament.ui.fragment.AbstractTournamentFragment
import net.gearmaniacs.tournament.ui.fragment.NewTournamentDialog
import net.gearmaniacs.tournament.ui.fragment.nav.InfoFragment
import net.gearmaniacs.tournament.ui.fragment.nav.LeaderboardFragment
import net.gearmaniacs.tournament.ui.fragment.nav.MatchFragment
import net.gearmaniacs.tournament.ui.fragment.nav.TeamFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel
import javax.inject.Qualifier

@AndroidEntryPoint
class TournamentActivity : AppCompatActivity() {

    private lateinit var binding: TournamentActivityBinding
    private val viewModel by viewModels<TournamentViewModel>()
    private val fragments by lazy {
        listOf(
            loadFragment(InfoFragment),
            loadFragment(TeamFragment),
            loadFragment(MatchFragment),
            loadFragment(LeaderboardFragment)
        )
    }
    private lateinit var activeFragment: AbstractTournamentFragment

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = TournamentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Make sure the data from the intent is not null
        intent.getStringExtra(ARG_TOURNAMENT_KEY)
            ?: throw IllegalArgumentException(ARG_TOURNAMENT_KEY)

        // Setup Fragments
        activeFragment = fragments.first()
        savedInstanceState?.let { bundle ->
            val restoredTag = bundle.getString(SAVED_FRAGMENT_INDEX)

            activeFragment =
                fragments.firstOrNull { it.getFragmentTag() == restoredTag } ?: fragments.first()
        }

        if (savedInstanceState == null) {

            // Add all the fragments first time the activity is created
            supportFragmentManager.commit {
                fragments.forEach {
                    add(R.id.layout_fragment, it, it.getFragmentTag())

                    // Hide all fragments except the active one
                    if (activeFragment.getFragmentTag() != it.getFragmentTag())
                        hide(it)
                }
            }
        }

        updateFab(activeFragment.getFragmentTag())

        binding.fab.setOnClickListener {
            activeFragment.fabClickListener()
        }

        // Setup Navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            val oldFragment = activeFragment
            val newFragment = fragments[it.order]

            if (oldFragment.getFragmentTag() != newFragment.getFragmentTag()) {
                updateFab(newFragment.getFragmentTag())

                supportFragmentManager.commit {
                    hide(oldFragment)
                    show(newFragment)
                }
                activeFragment = newFragment
                invalidateOptionsMenu()

                true
            } else false
        }

        binding.bottomNavigation.doOnPreDraw { appBar ->
            binding.layoutFragment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = appBar.height)
            }
        }

        // Observe Live Data
        observe(viewModel.getCurrentTournamentLiveData()) { thisTournament: Tournament? ->
            if (thisTournament == null) {
                // Means the Tournament has been deleted so we should close the activity
                finish()
                return@observe
            }

            supportActionBar?.title = thisTournament.name
        }

        observe(viewModel.getTeamsLiveData()) {
            if (it != null)
                teamsList = it
        }

        observe(viewModel.getMatchesLiveData()) {
            if (it != null)
                matchesList = it
        }
    }

    private fun loadFragment(companion: AbstractTournamentFragment.ICompanion): AbstractTournamentFragment =
        supportFragmentManager.findFragmentByTag(companion.fragmentTag) as? AbstractTournamentFragment?
            ?: companion.newInstance()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVED_FRAGMENT_INDEX, activeFragment.getFragmentTag())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tournament, menu)
        val activeTag = activeFragment.getFragmentTag()

        menu.findItem(R.id.action_opr_info).isVisible = activeTag == LeaderboardFragment.fragmentTag

        return super.onCreateOptionsMenu(menu)
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
                viewModel.getCurrentTournamentLiveData().value?.name?.let {
                    intent.putExtra(Intent.EXTRA_TITLE, it)
                }

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

    override fun onStart() {
        super.onStart()
        viewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    private fun updateFab(newFragmentTag: String) {
        val fab = binding.fab

        if (newFragmentTag == InfoFragment.fragmentTag || newFragmentTag == LeaderboardFragment.fragmentTag) {
            fab.hide() // InfoFragment and LeaderboardFragment don't have a FAB
            return
        }

        fab.show()
    }

    private fun changeTournamentName() {
        val dialogFragment = NewTournamentDialog()
        dialogFragment.actionButtonStringRes = R.string.action_update
        dialogFragment.defaultName = viewModel.getCurrentTournamentLiveData().value?.name

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

        private const val SAVED_FRAGMENT_INDEX = "tournament_key"

        fun startActivity(context: Context, userTeam: UserTeam, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java).apply {
                putExtra(ARG_USER, userTeam)
                putExtra(ARG_TOURNAMENT_KEY, tournament.key)
            }

            context.startActivity(intent)
        }
    }

    /*
     * Provide the tournament key passed by the intent
     */
    @Qualifier
    @Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    internal annotation class TournamentKey

    @TournamentKey
    internal fun getTournamentKey() = intent.getStringExtra(ARG_TOURNAMENT_KEY)!!

    @Module
    @InstallIn(ActivityComponent::class)
    internal object TournamentKeyModule {

        @Provides
        @ActivityScoped
        @TournamentKey
        fun provideTournamentKey(@ActivityContext activity: Context): String {
            return (activity as TournamentActivity).getTournamentKey()
        }
    }
}
