package net.gearmaniacs.ftcscouting.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.ActivityMainBinding
import net.gearmaniacs.ftcscouting.ui.adapter.TournamentAdapter
import net.gearmaniacs.ftcscouting.viewmodel.MainViewModel
import net.gearmaniacs.login.ui.activity.LoginActivity
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.fragment.TournamentDialogFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), RecyclerViewItemListener<Tournament> {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var userData: UserData

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (appPreferences.firstStartUp.get()) {
            // If the user is already logged in
            if (Firebase.isLoggedIn)
                appPreferences.firstStartUp.set(false)
            else
                return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar)

        val adapter = TournamentAdapter(this)

        val recyclerView = binding.content.rvTournament
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )

        binding.fabNewTournament.setOnClickListener {
            val dialogFragment = TournamentDialogFragment()
            dialogFragment.actionButtonStringRes = R.string.action_create

            dialogFragment.actionButtonListener = { name ->
                val tournamentName = name.trim()

                if (tournamentName.isNotEmpty())
                    viewModel.createNewTournament(tournamentName)
            }
            dialogFragment.show(supportFragmentManager, dialogFragment.tag)
        }

        binding.bottomAppBar.doOnPreDraw { appBar ->
            recyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = appBar.height)
            }
        }

        observe(viewModel.getUserLiveData()) {
            if (it != null)
                userData = it
        }

        observe(viewModel.getTournamentsLiveData()) { list ->
            adapter.submitList(list ?: emptyList())
        }
    }

    override fun onStart() {
        super.onStart()
        if (!appPreferences.seenIntro.get()) {
            val intent = Intent(this, IntroActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_INTRO)
            return
        }
        if (appPreferences.firstStartUp.get()) {
            startActivity<LoginActivity>()
            return
        }

        viewModel.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        menu.findItem(R.id.action_sign_in).isVisible = !Firebase.isLoggedIn
        menu.findItem(R.id.action_sign_out).isVisible = Firebase.isLoggedIn

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_about -> {
            startActivity<AboutActivity>()
            true
        }
        R.id.action_account -> {
            TeamInfoActivity.startActivity(this, userData)
            true
        }
        R.id.action_sign_in -> {
            startActivity<LoginActivity>()
            finish()
            true
        }
        R.id.action_sign_out -> {
            Firebase.auth.signOut()
            viewModel.signOut(applicationContext)
            startActivity<LoginActivity>()
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                appPreferences.seenIntro.set(true)
            } else {
                finish()
            }
        }
    }

    override fun onClickListener(item: Tournament) {
        try {
            TournamentActivity.startActivity(this, userData, item)
        } catch (e: IndexOutOfBoundsException) {
        }
    }

    override fun onLongClickListener(item: Tournament) {
        try {
            AlertDialog.Builder(this)
                .setTitle(R.string.delete_tournament)
                // TODO Add message for offline mode
                .setMessage(R.string.delete_tournament_desc)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewModel.deleteTournament(item)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } catch (e: IndexOutOfBoundsException) {
        }
    }

    private companion object {
        private const val REQUEST_CODE_INTRO = 100
    }
}
