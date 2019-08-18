package net.gearmaniacs.ftcscouting.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import net.gearmaniacs.core.extensions.getViewModel
import net.gearmaniacs.core.extensions.lazyFast
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.ui.adapter.TournamentAdapter
import net.gearmaniacs.ftcscouting.viewmodel.MainViewModel
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.fragment.TournamentDialogFragment
import net.gearmaniacs.tournament.utils.RecyclerViewItemListener

class MainActivity : AppCompatActivity(), RecyclerViewItemListener {

    private val viewModel by lazyFast { getViewModel<MainViewModel>() }
    private val adapter by lazyFast { TournamentAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(bottom_app_bar)

        rv_tournament.adapter = adapter
        rv_tournament.setHasFixedSize(true)
        rv_tournament.layoutManager = LinearLayoutManager(this)
        rv_tournament.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        fab_new_tournament.setOnClickListener {
            val dialogFragment = TournamentDialogFragment()
            dialogFragment.actionButtonStringRes = R.string.action_create

            dialogFragment.actionButtonListener = { name ->
                if (name.isNotBlank())
                    viewModel.createNewTournament(name)
            }
            dialogFragment.show(supportFragmentManager, dialogFragment.tag)
        }

        bottom_app_bar.doOnPreDraw { appBar ->
            rv_tournament.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = appBar.height)
            }
        }

        observeNonNull(viewModel.tournamentData) {
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
            viewModel.currentUser?.let {
                AccountActivity.startActivity(this, it)
            }
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
        viewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    override fun onClickListener(position: Int) {
        try {
            val item = adapter.getItem(position)
            val user = viewModel.currentUser ?: return
            TournamentActivity.startActivity(this, user, item)
        } catch (e: IndexOutOfBoundsException) {
        }
    }

    override fun onLongClickListener(position: Int) {
        try {
            val item = adapter.getItem(position)

            AlertDialog.Builder(this)
                .setTitle(R.string.delete_tournament)
                .setMessage(R.string.delete_tournament_desc)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewModel.deleteTournament(item)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } catch (e: IndexOutOfBoundsException) {
        }
    }
}
