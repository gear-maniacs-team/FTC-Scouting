package net.gearmaniacs.tournament.ui.fragment

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.view.EmptyRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.TeamAdapter
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class TeamFragment : TournamentFragment(R.layout.fragment_recycler_view),
    RecyclerViewItemListener {

    companion object {
        const val TAG = "TeamFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var adapter: TeamAdapter

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        emptyView.setText(R.string.empty_tab_teams)

        adapter = TeamAdapter(this)

        recyclerView.emptyView = emptyView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }
        })

        activity.observeNonNull(viewModel.getTeamsLiveData()) {
            adapter.submitList(it)
        }
    }

    override fun fabClickListener() {
        val activity = activity ?: return

        val dialog = TeamEditDialog.newInstance()
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, TeamEditDialog.TAG)
    }

    override fun getFragmentTag() = TAG

    override fun onClickListener(position: Int) {
        val activity = activity ?: return
        val team = adapter.getItem(position)

        val dialog = TeamEditDialog.newInstance(team)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, TeamEditDialog.TAG)
    }

    override fun onLongClickListener(position: Int) {
        val activity = activity ?: return
        val key = adapter.getItem(position).key ?: return

        AlertDialog.Builder(activity)
            .setTitle(R.string.delete_team)
            .setMessage(R.string.delete_team_desc)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteTeam(key)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
