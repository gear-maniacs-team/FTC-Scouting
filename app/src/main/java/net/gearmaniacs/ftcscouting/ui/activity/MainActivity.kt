package net.gearmaniacs.ftcscouting.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.alertDialog
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.MainActivityBinding
import net.gearmaniacs.ftcscouting.ui.adapter.TournamentAdapter
import net.gearmaniacs.ftcscouting.ui.fragment.MainMenuDialog
import net.gearmaniacs.ftcscouting.viewmodel.MainViewModel
import net.gearmaniacs.login.ui.activity.LoginActivity
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.fragment.NewTournamentDialog
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), RecyclerViewItemListener<Tournament> {

    private lateinit var binding: MainActivityBinding
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var userTeam: UserTeam

    private var isLoggedIn = false

    @Inject
    lateinit var appPreferences: AppPreferences

    init {
        lifecycleScope.launchWhenCreated {
            appPreferences.isLoggedInFlow.collect { isLoggedIn = it }
        }

        lifecycleScope.launchWhenStarted {
            val hasSeenIntro = appPreferences.seenIntroFlow.first()

            if (!hasSeenIntro) {
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        lifecycleScope.launch { appPreferences.setSeenIntro(true) }
                    } else {
                        finish()
                    }
                }.launch(Intent(this@MainActivity, IntroActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
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

        binding.bottomAppBar.setNavigationOnClickListener {
            val dialog = MainMenuDialog.newInstance()
            val transaction = supportFragmentManager.beginTransaction()
            dialog.show(transaction, null)
        }

        binding.fabNewTournament.setOnClickListener {
            val dialogFragment = NewTournamentDialog()
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

        observe(viewModel.getUserTeamLiveData()) {
            if (it != null)
                userTeam = it
        }

        observe(viewModel.getTournamentsLiveData()) { list ->
            adapter.submitList(list ?: emptyList())
        }
    }

    override fun onStart() {
        super.onStart()
        if (isLoggedIn)
            viewModel.startListening()
        else {
            if (Firebase.isLoggedIn) {
                lifecycleScope.launch { appPreferences.setLoggedIn(true) }
            } else
                startActivity<LoginActivity>()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    override fun onClickListener(item: Tournament) {
        TournamentActivity.startActivity(this, userTeam, item)
    }

    override fun onLongClickListener(item: Tournament) {
        val message =
            if (Firebase.isLoggedIn) R.string.delete_tournament_desc else R.string.delete_tournament_desc_offline

        alertDialog {
            setTitle(R.string.delete_tournament)
            setMessage(message)
            setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteTournament(item)
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }
}
