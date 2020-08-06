package net.gearmaniacs.tournament.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.ActivityTournamentBinding
import net.gearmaniacs.tournament.ui.fragment.AbstractTournamentFragment
import net.gearmaniacs.tournament.ui.fragment.TournamentDialogFragment
import net.gearmaniacs.tournament.ui.fragment.nav.AnalyticsFragment
import net.gearmaniacs.tournament.ui.fragment.nav.InfoFragment
import net.gearmaniacs.tournament.ui.fragment.nav.MatchFragment
import net.gearmaniacs.tournament.ui.fragment.nav.TeamFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel
import javax.inject.Qualifier

@AndroidEntryPoint
class TournamentActivity : AppCompatActivity() {

    companion object {
        const val ARG_TOURNAMENT_KEY = "tournament_key"
        const val ARG_USER = "user"

        private const val SAVED_FRAGMENT_INDEX = "tournament_key"

        private const val SPREADSHEET_LOAD_REQUEST_CODE = 1
        private const val SPREADSHEET_SAVE_REQUEST_CODE = 2

        fun startActivity(context: Context, userData: UserData, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java).apply {
                putExtra(ARG_USER, userData)
                putExtra(ARG_TOURNAMENT_KEY, tournament.key)
            }

            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityTournamentBinding
    private val viewModel by viewModels<TournamentViewModel>()
    private val fragments by lazy {
        listOf(
            loadFragment(InfoFragment),
            loadFragment(TeamFragment),
            loadFragment(MatchFragment),
            loadFragment(AnalyticsFragment)
        )
    }
    private lateinit var activeFragment: AbstractTournamentFragment

    private var teamsList = emptyList<Team>()
    private var matchesList = emptyList<Match>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTournamentBinding.inflate(layoutInflater)
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
            teamsList = it ?: emptyList()
            Log.i("Teams1", teamsList.size.toString())
        }

        observe(viewModel.getMatchesLiveData()) {
            matchesList = it ?: emptyList()
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

        menu.findItem(R.id.action_opr_info).isVisible = activeTag == AnalyticsFragment.fragmentTag

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

                startActivityForResult(intent, SPREADSHEET_SAVE_REQUEST_CODE)
            }
            R.id.action_import -> {
                val intent = getSpreadsheetIntent(Intent.ACTION_OPEN_DOCUMENT)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                startActivityForResult(intent, SPREADSHEET_LOAD_REQUEST_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == SPREADSHEET_LOAD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->
                viewModel.importFromSpreadSheet(documentUri, teamsList, matchesList)
            }
        }

        if (requestCode == SPREADSHEET_SAVE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->
                viewModel.exportToSpreadsheet(documentUri, teamsList, matchesList)
            }
        }
    }

    private fun updateFab(newFragmentTag: String) {
        val fab = binding.fab

        if (newFragmentTag == InfoFragment.fragmentTag || newFragmentTag == AnalyticsFragment.fragmentTag) {
            fab.hide() // InfoFragment and AnalyticsFragment don't have a FAB
            return
        }

        fab.show()
    }

    private fun changeTournamentName() {
        val dialogFragment = TournamentDialogFragment()
        dialogFragment.actionButtonStringRes = R.string.action_update
        dialogFragment.defaultName = viewModel.getCurrentTournamentLiveData().value?.name

        dialogFragment.actionButtonListener = { newName ->
            viewModel.updateTournamentName(newName)
        }

        dialogFragment.show(supportFragmentManager, dialogFragment.tag)
    }

    private fun deleteTournament() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_tournament)
            .setMessage(R.string.delete_tournament_desc)
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

    /// Provide the tournament key passed by the intent

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
