package net.gearmaniacs.ftcscouting.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_tournament.*
import kotlinx.coroutines.Runnable
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.User
import net.gearmaniacs.ftcscouting.ui.fragments.TournamentDialogFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.AnalyticsFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.InfoFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.MatchFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.TeamsFragment
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.ftcscouting.viewmodel.TournamentViewModel

class TournamentActivity : AppCompatActivity() {

    companion object {
        private const val ARG_TOURNAMENT_KEY = "tournament_key"
        const val ARG_USER = "user"
        private const val ARG_TOURNAMENT_NAME = "tournament_name"

        private const val SAVED_FRAGMENT_INDEX = "tournament_key"

        fun startActivity(context: Context, user: User, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java)
            intent.putExtra(ARG_TOURNAMENT_KEY, tournament.key)
            intent.putExtra(ARG_USER, user)
            intent.putExtra(ARG_TOURNAMENT_NAME, tournament.name)
            context.startActivity(intent)
        }
    }

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

        rv_main.setHasFixedSize(true)
        rv_main.layoutManager = LinearLayoutManager(this)
        rv_main.setItemViewCacheSize(5)

        bottom_navigation.setOnNavigationItemSelectedListener {
            val newFragment = fragments[it.order]

            if (activeFragment.getFragmentTag() != newFragment.getFragmentTag()) {
                updateFab(activeFragment.getFragmentTag(), newFragment.getFragmentTag())

                supportFragmentManager.beginTransaction()
                    .remove(activeFragment)
                    .add(newFragment, newFragment.getFragmentTag())
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
            .add(activeFragment, activeFragment.getFragmentTag())
            .commit()

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

    private fun updateFab(oldFragmentTag: String, newFragmentTag: String) {
        val fabDrawable = if (newFragmentTag == AnalyticsFragment.TAG) R.drawable.ic_refresh else R.drawable.ic_add

        if (oldFragmentTag == InfoFragment.TAG) {
            fab.setImageResource(fabDrawable)
            fab.show()
            return
        } else if (newFragmentTag == InfoFragment.TAG) {
            fab.hide()
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

        val handler = Handler()
        handler.postDelayed(Runnable {
            fab.setImageDrawable(getDrawable(fabDrawable))
        }, animationDuration)
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
}
