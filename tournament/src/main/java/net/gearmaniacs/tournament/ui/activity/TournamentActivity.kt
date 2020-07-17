package net.gearmaniacs.tournament.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.ActivityTournamentBinding
import net.gearmaniacs.tournament.ui.fragment.TournamentDialogFragment
import net.gearmaniacs.tournament.ui.fragment.TournamentFragment
import net.gearmaniacs.tournament.ui.fragment.nav.AnalyticsFragment
import net.gearmaniacs.tournament.ui.fragment.nav.InfoFragment
import net.gearmaniacs.tournament.ui.fragment.nav.MatchFragment
import net.gearmaniacs.tournament.ui.fragment.nav.TeamFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
class TournamentActivity : AppCompatActivity() {

    companion object {
        private const val ARG_TOURNAMENT_KEY = "tournament_key"
        const val ARG_USER = "user"
        private const val ARG_TOURNAMENT_NAME = "tournament_name"

        private const val SAVED_FRAGMENT_INDEX = "tournament_key"

        private const val SPREADSHEET_LOAD_REQUEST_CODE = 1
        private const val SPREADSHEET_SAVE_REQUEST_CODE = 2

        fun startActivity(context: Context, user: User?, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java).apply {
                putExtra(ARG_USER, user)
                putExtra(ARG_TOURNAMENT_KEY, tournament.key)
                putExtra(ARG_TOURNAMENT_NAME, tournament.name)
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
    private lateinit var activeFragment: TournamentFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTournamentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Make sure the data from the intent is not null
        viewModel.tournamentKey = intent.getStringExtra(ARG_TOURNAMENT_KEY)
            ?: throw IllegalArgumentException(ARG_TOURNAMENT_KEY)
        val tournamentName = intent.getStringExtra(ARG_TOURNAMENT_NAME)
            ?: throw IllegalArgumentException(ARG_TOURNAMENT_NAME)

        viewModel.setDefaultName(tournamentName)

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

        updateFab(fragments.first().getFragmentTag(), activeFragment.getFragmentTag())

        binding.fab.setOnClickListener {
            activeFragment.fabClickListener()
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            val oldFragment = activeFragment
            val newFragment = fragments[it.order]

            if (oldFragment.getFragmentTag() != newFragment.getFragmentTag()) {
                updateFab(oldFragment.getFragmentTag(), newFragment.getFragmentTag())

                supportFragmentManager.commit {
                    hide(oldFragment)
                    show(newFragment)
                }
                activeFragment = newFragment
                invalidateOptionsMenu()

                true
            } else false
        }

        observe(viewModel.getNameLiveData()) { name ->
            if (name == null) {
                // Means the Tournament has been deleted so we should close the activity
                finish()
                return@observe
            }

            supportActionBar?.title = name
        }
    }

    private fun loadFragment(companion: TournamentFragment.ICompanion): TournamentFragment =
        supportFragmentManager.findFragmentByTag(companion.fragmentTag) as? TournamentFragment?
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
                        viewModel.addTeamsFromMatches()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            R.id.action_export -> {
                val intent = getSpreadsheetIntent(Intent.ACTION_CREATE_DOCUMENT)
                intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                intent.putExtra(Intent.EXTRA_TITLE, viewModel.getNameLiveData().value)

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
                viewModel.importFromSpreadSheet(documentUri)
            }
        }

        if (requestCode == SPREADSHEET_SAVE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->
                viewModel.exportToSpreadsheet(documentUri)
            }
        }
    }

    private fun updateFab(oldFragmentTag: String, newFragmentTag: String) {
        val fab = binding.fab
        val newDrawable = getDrawable(
            if (newFragmentTag == AnalyticsFragment.fragmentTag)
                R.drawable.ic_refresh
            else
                R.drawable.ic_add
        )

        if (fab.isOrWillBeHidden) {
            fab.setImageDrawable(newDrawable)
            fab.show()
            return
        }

        if (newFragmentTag == InfoFragment.fragmentTag) {
            fab.hide() // InfoFragment shouldn't have a visible FAB
            return
        }

        if (oldFragmentTag != AnalyticsFragment.fragmentTag && newFragmentTag != AnalyticsFragment.fragmentTag) return

        val animationDuration = 150L
        val relativeToSelfAnim = Animation.RELATIVE_TO_SELF

        val fabAnimation =
            ScaleAnimation(1f, 0f, 1f, 0f, relativeToSelfAnim, 0.5f, relativeToSelfAnim, 0.5f)
        fabAnimation.duration = animationDuration
        fabAnimation.interpolator = AccelerateInterpolator()
        fabAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) = Unit

            override fun onAnimationRepeat(animation: Animation) = Unit

            override fun onAnimationEnd(animation: Animation) {
                val expand = ScaleAnimation(
                    0f,
                    1f,
                    0f,
                    1f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                expand.duration = animationDuration
                expand.interpolator = DecelerateInterpolator()
                fab.startAnimation(expand)
            }
        })

        GlobalScope.launch(Dispatchers.Main.immediate) {
            fab.startAnimation(fabAnimation)
            delay(animationDuration)
            fab.setImageDrawable(newDrawable)
        }
    }

    private fun changeTournamentName() {
        val dialogFragment = TournamentDialogFragment()
        dialogFragment.actionButtonStringRes = R.string.action_update
        dialogFragment.defaultName = viewModel.getNameLiveData().value

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
}
