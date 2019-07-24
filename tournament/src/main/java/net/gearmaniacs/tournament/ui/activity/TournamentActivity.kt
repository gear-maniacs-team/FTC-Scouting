package net.gearmaniacs.tournament.ui.activity

import android.Manifest
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
import kotlinx.android.synthetic.main.activity_tournament.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.checkRuntimePermission
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.fragment.AnalyticsFragment
import net.gearmaniacs.tournament.ui.fragment.InfoFragment
import net.gearmaniacs.tournament.ui.fragment.MatchFragment
import net.gearmaniacs.tournament.ui.fragment.TeamsFragment
import net.gearmaniacs.tournament.ui.fragment.TournamentDialogFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel
import net.theluckycoder.materialchooser.Chooser
import java.io.File

class TournamentActivity : AppCompatActivity() {

    private val viewModel by viewModels<TournamentViewModel>()

    private val fragments = listOf(InfoFragment(), TeamsFragment(), MatchFragment(), AnalyticsFragment())
    private var activeFragment = fragments.first()
    private var tournamentKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Make sure the data from the intent is not null
        tournamentKey = intent.getStringExtra(ARG_TOURNAMENT_KEY)
            ?: throw IllegalArgumentException(ARG_TOURNAMENT_KEY)
        viewModel.tournamentKey = tournamentKey
        val tournamentName = intent.getStringExtra(ARG_TOURNAMENT_NAME)
            ?: throw IllegalArgumentException(ARG_TOURNAMENT_NAME)

        viewModel.setDefaultName(tournamentName)

        fab.setOnClickListener {
            activeFragment.fabClickListener()
        }

        bottom_navigation.setOnNavigationItemSelectedListener {
            val newFragment = fragments[it.order]

            if (activeFragment.getFragmentTag() != newFragment.getFragmentTag()) {
                updateFab(activeFragment.getFragmentTag(), newFragment.getFragmentTag())

                supportFragmentManager.beginTransaction()
                    .replace(R.id.layout_fragment, newFragment, newFragment.getFragmentTag())
                    .commit()
                activeFragment = newFragment

                true
            } else false
        }

        // Setup Fragments
        savedInstanceState?.let {
            activeFragment = fragments[it.getInt(SAVED_FRAGMENT_INDEX)]
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout_fragment, activeFragment, activeFragment.getFragmentTag())
            .commit()
        updateFab(fragments.first().getFragmentTag(), activeFragment.getFragmentTag())

        observe(viewModel.nameData) { name ->
            if (name == null) {
                // Means the Tournament has been deleted so we can close the activity
                finish()
                return@observe
            }

            supportActionBar?.title = name
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tournament, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
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
                checkRuntimePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                viewModel.exportToSpreadsheet(applicationContext)
            }
            R.id.action_import -> {
                Chooser(
                    activity = this,
                    requestCode = SPREADSHEET_REQUEST_CODE,
                    fileExtension = "xls",
                    useNightTheme = true
                ).start()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPREADSHEET_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val path = data?.getStringExtra(Chooser.RESULT_PATH) ?: return
            viewModel.importFromSpreadSheet(File(path))
        }
    }

    private fun updateFab(oldFragmentTag: String, newFragmentTag: String) {
        val newDrawable = getDrawable(
            if (newFragmentTag == AnalyticsFragment.TAG)
                R.drawable.ic_refresh
            else
                R.drawable.ic_add
        )

        if (fab.isOrWillBeHidden) {
            fab.setImageDrawable(newDrawable)
            fab.show()
            return
        }

        if (newFragmentTag == InfoFragment.TAG) {
            fab.hide() // InfoFragment shouldn't have a visible FAB
            return
        }

        if (oldFragmentTag != AnalyticsFragment.TAG && newFragmentTag != AnalyticsFragment.TAG) return

        val animationDuration = 150L

        val fabAnimation =
            ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fabAnimation.duration = animationDuration
        fabAnimation.interpolator = AccelerateInterpolator()
        fabAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) = Unit
            override fun onAnimationRepeat(animation: Animation) = Unit

            override fun onAnimationEnd(animation: Animation) {
                val expand =
                    ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                expand.duration = animationDuration
                expand.interpolator = DecelerateInterpolator()
                fab.startAnimation(expand)
            }
        })

        fab.startAnimation(fabAnimation)

        GlobalScope.launch(Dispatchers.Main.immediate) {
            delay(animationDuration)
            fab.setImageDrawable(newDrawable)
        }
    }

    private fun changeTournamentName() {
        val dialogFragment = TournamentDialogFragment()
        dialogFragment.actionButtonStringRes = R.string.action_update
        dialogFragment.defaultName = viewModel.nameData.value

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

    companion object {
        private const val ARG_TOURNAMENT_KEY = "tournament_key"
        const val ARG_USER = "user"
        private const val ARG_TOURNAMENT_NAME = "tournament_name"

        private const val SAVED_FRAGMENT_INDEX = "tournament_key"

        private const val SPREADSHEET_REQUEST_CODE = 1

        fun startActivity(context: Context, user: User, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java)
            intent.putExtra(ARG_TOURNAMENT_KEY, tournament.key)
            intent.putExtra(ARG_USER, user)
            intent.putExtra(ARG_TOURNAMENT_NAME, tournament.name)
            context.startActivity(intent)
        }
    }
}
