package net.gearmaniacs.ftcscouting.ui.activity

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
import com.google.firebase.auth.FirebaseAuth
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.ActivityMainBinding
import net.gearmaniacs.ftcscouting.ui.adapter.TournamentAdapter
import net.gearmaniacs.ftcscouting.viewmodel.MainViewModel
import net.gearmaniacs.login.ui.activity.LoginActivity
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.fragment.TournamentDialogFragment
import net.gearmaniacs.tournament.utils.RecyclerViewItemListener

class MainActivity : AppCompatActivity(), RecyclerViewItemListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val adapter = TournamentAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar)

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

        observeNonNull(viewModel.getTournamentsData()) {
            adapter.submitList(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_about -> {
            startActivity<AboutActivity>()
            true
        }
        R.id.action_account -> {
            TeamInfoActivity.startActivity(this, viewModel.userData)
            true
        }
        R.id.action_sign_out -> {
            FirebaseAuth.getInstance().signOut()
            startActivity<LoginActivity>()
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity<LoginActivity>()
            finish()
            return
        }
        viewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    override fun onClickListener(position: Int) {
        try {
            val tournament = adapter.getItem(position)
            TournamentActivity.startActivity(this, viewModel.userData, tournament)
        } catch (e: IndexOutOfBoundsException) {
        }
    }

    override fun onLongClickListener(position: Int) {
        try {
            val tournament = adapter.getItem(position)

            AlertDialog.Builder(this)
                .setTitle(R.string.delete_tournament)
                .setMessage(R.string.delete_tournament_desc)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewModel.deleteTournament(tournament)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } catch (e: IndexOutOfBoundsException) {
        }
    }
}
