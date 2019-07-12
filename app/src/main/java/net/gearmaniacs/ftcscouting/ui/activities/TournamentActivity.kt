package net.gearmaniacs.ftcscouting.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_tournament.*
import kotlinx.coroutines.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.data.Tournament
import net.gearmaniacs.ftcscouting.ui.fragments.TournamentDialogFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.AnalyticsFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.MatchFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.TeamsFragment
import net.gearmaniacs.ftcscouting.ui.fragments.tournaments.TournamentsFragment
import net.gearmaniacs.ftcscouting.ui.viewmodel.TournamentViewModel
import net.gearmaniacs.ftcscouting.utils.architecture.getViewModel
import net.gearmaniacs.ftcscouting.utils.architecture.observe
import net.gearmaniacs.ftcscouting.utils.extensions.getTextOrEmpty
import net.gearmaniacs.ftcscouting.utils.extensions.lazyFast
import net.gearmaniacs.ftcscouting.utils.extensions.toIntOrDefault

class TournamentActivity : AppCompatActivity() {

    companion object {
        private const val ARG_TOURNAMENT_KEY = "tournament_key"
        private const val ARG_TOURNAMENT_NAME = "tournament_name"

        fun startActivity(context: Context, tournament: Tournament) {
            val intent = Intent(context, TournamentActivity::class.java)
            intent.putExtra(ARG_TOURNAMENT_KEY, tournament.key)
            intent.putExtra(ARG_TOURNAMENT_NAME, tournament.name)
            context.startActivity(intent)
        }
    }

    private var tournamentKey = ""
    private var tournamentsFragment: TournamentsFragment? = null
    private val viewModel by lazyFast { getViewModel<TournamentViewModel>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tournamentKey = intent.getStringExtra(ARG_TOURNAMENT_KEY) ?: throw IllegalArgumentException(ARG_TOURNAMENT_KEY)
        viewModel.tournamentKey = tournamentKey
        val tournamentName = intent.getStringExtra(ARG_TOURNAMENT_NAME)
            ?: throw IllegalArgumentException(ARG_TOURNAMENT_NAME)
        viewModel.setDefaultName(tournamentName)

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_teams ->
                    if (viewModel.fragmentTag != TeamsFragment.TAG) replaceFragment(TeamsFragment())
                R.id.nav_matches ->
                    if (viewModel.fragmentTag != MatchFragment.TAG) replaceFragment(MatchFragment())
                R.id.nav_analytics ->
                    if (viewModel.fragmentTag != AnalyticsFragment.TAG) replaceFragment(AnalyticsFragment())
            }
            true
        }

        fab.setOnClickListener {
            tournamentsFragment?.fabClickListener()
        }

        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this)

        if (tournamentsFragment == null) {
            tournamentsFragment = when (viewModel.fragmentTag) {
                TeamsFragment.TAG -> TeamsFragment()
                MatchFragment.TAG -> MatchFragment()
                AnalyticsFragment.TAG -> AnalyticsFragment()
                else -> throw IllegalArgumentException("Invalid Fragment Tag")
            }
        }

        tournamentsFragment?.let {
            replaceFragment(it)
        }

        observe(viewModel.nameData) { name ->
            if (name == null) { // Means the Tournament has been deleted
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
                val editText = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    setSingleLine(true)
                    keyListener = DigitsKeyListener.getInstance("0123456789., ")
                }

                AlertDialog.Builder(this)
                    .setTitle("Add Teams")
                    .setMessage("Add Teams Numbers separated by a comma or a space")
                    .setView(editText)
                    .setPositiveButton(R.string.action_add) { _, _ ->
                        val text = editText.getTextOrEmpty()

                        val teams = text.splitToSequence(',', '.', ' ')
                            .map { it.toIntOrDefault() }
                            .toList()

                        viewModel.addTeams(teams)
                        bottom_navigation.selectedItemId = R.id.nav_teams
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

    private fun replaceFragment(fragment: TournamentsFragment) {
        updateFab(viewModel.fragmentTag, fragment.getFragmentTag())

        supportFragmentManager.beginTransaction().apply {
            tournamentsFragment?.let { remove(it) }
            add(fragment, viewModel.fragmentTag)
            commit()
        }

        tournamentsFragment = fragment
        viewModel.fragmentTag = fragment.getFragmentTag()
    }

    private fun updateFab(oldFragmentTag: String, newFragmentTag: String) {
        if (oldFragmentTag != AnalyticsFragment.TAG && newFragmentTag != AnalyticsFragment.TAG) return

        val refreshIcon = if (newFragmentTag == AnalyticsFragment.TAG) R.drawable.ic_refresh else R.drawable.ic_add

        val fabAnimation =
            ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fabAnimation.duration = 150
        fabAnimation.interpolator = AccelerateInterpolator()
        fabAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) = Unit
            override fun onAnimationRepeat(animation: Animation) = Unit

            override fun onAnimationEnd(animation: Animation) {
                val expand =
                    ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                expand.duration = 150
                expand.interpolator = DecelerateInterpolator()
                fab.startAnimation(expand)
            }
        })

        fab.startAnimation(fabAnimation)

        val handler = Handler()
        handler.postDelayed(Runnable {
            fab.setImageDrawable(getDrawable(refreshIcon))
        }, 150)
    }

    private fun changeTournamentName() {
        val dialogFragment = TournamentDialogFragment()
        dialogFragment.actionButtonStringRes = R.string.action_update
        dialogFragment.defaultName = viewModel.nameData.value

        dialogFragment.actionButtonListener = { newName ->
            if (newName.isNotBlank()) {
                viewModel.currentUserReference
                    .child("tournaments")
                    .child(tournamentKey)
                    .setValue(newName)
            }
        }

        dialogFragment.show(supportFragmentManager, dialogFragment.tag)
    }

    private fun deleteTournament() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_tournament)
            .setMessage(R.string.delete_tournament_desc)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.currentUserReference
                    .child("tournaments")
                    .child(tournamentKey)
                    .removeValue()

                viewModel.currentUserReference
                    .child("data")
                    .child(tournamentKey)
                    .removeValue()

                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
