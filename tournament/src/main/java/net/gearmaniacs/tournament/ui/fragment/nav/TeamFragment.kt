package net.gearmaniacs.tournament.ui.fragment.nav

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.view.EmptyRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener
import net.gearmaniacs.tournament.ui.adapter.SearchAdapter
import net.gearmaniacs.tournament.ui.adapter.TeamAdapter
import net.gearmaniacs.tournament.ui.fragment.AbstractTournamentFragment
import net.gearmaniacs.tournament.ui.fragment.TeamEditDialog
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class TeamFragment : AbstractTournamentFragment(R.layout.fragment_recycler_view),
    RecyclerViewItemListener<Team> {

    private val viewModel by activityViewModels<TournamentViewModel>()

    override fun onInflateView(view: View) {
        val activity = requireActivity()

        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        emptyView.setText(R.string.empty_tab_teams)

        val searchAdapter = SearchAdapter {
            viewModel.performTeamsSearch(it?.toString())
        }
        val teamAdapter = TeamAdapter(this)

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

            setEmptyView(emptyView)
        }
        recyclerView.adapter = ConcatAdapter(searchAdapter, teamAdapter)

        activity.observeNonNull(viewModel.getTeamsFilteredLiveData()) {
            teamAdapter.submitList(it)
        }
    }

    override fun fabClickListener() {
        val activity = activity ?: return

        val dialog = TeamEditDialog.newInstance()
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, TeamEditDialog.TAG)
    }

    override fun getFragmentTag() = fragmentTag

    override fun onClickListener(item: Team) {
        val activity = activity ?: return

        val dialog = TeamEditDialog.newInstance(item)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, TeamEditDialog.TAG)
    }

    override fun onLongClickListener(item: Team) {
        val activity = activity ?: return
        val key = item.key

        AlertDialog.Builder(activity)
            .setTitle(R.string.delete_team)
            .setMessage(R.string.delete_team_desc)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteTeam(key)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object : ICompanion {
        override val fragmentTag = "TeamFragment"

        override fun newInstance() = TeamFragment()
    }
}
