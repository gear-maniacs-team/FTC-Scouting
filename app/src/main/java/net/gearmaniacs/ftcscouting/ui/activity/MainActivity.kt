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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.alertDialog
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.UserData
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

    private lateinit var userData: UserData

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!appPreferences.isLoggedIn.get()) {
            // If the user is already logged in
            if (Firebase.isLoggedIn)
                appPreferences.isLoggedIn.set(true)
            else
                return
        }

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
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    appPreferences.seenIntro.set(true)
                } else {
                    finish()
                }
            }.launch(Intent(this, IntroActivity::class.java))
            return
        }
        if (!appPreferences.isLoggedIn.get()) {
            startActivity<LoginActivity>()
            return
        }

        viewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    override fun onClickListener(item: Tournament) {
        TournamentActivity.startActivity(this, userData, item)
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
