package net.gearmaniacs.tournament.ui.fragment

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_tournament.*
import kotlinx.android.synthetic.main.fragment_recycler_view.view.*
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.TeamAdapter
import net.gearmaniacs.tournament.utils.DataRecyclerViewListener
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class TeamFragment : TournamentFragment(R.layout.fragment_recycler_view), DataRecyclerViewListener {

    companion object {
        const val TAG = "TeamFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var adapter: TeamAdapter

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val fab = activity.fab
        val recyclerView = view.recycler_view

        adapter = TeamAdapter(this)

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

        activity.observeNonNull(viewModel.teamsData) {
            adapter.submitList(it)
        }
    }

    override fun fabClickListener() {
        val activity = activity ?: return

        val dialog = TeamEditDialog()
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun getFragmentTag() = TAG

    override fun onEditItem(position: Int) {
        val activity = activity ?: return
        val team = adapter.getItem(position)

        val dialog = TeamEditDialog.newInstance(team)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun onDeleteItem(position: Int) {
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
