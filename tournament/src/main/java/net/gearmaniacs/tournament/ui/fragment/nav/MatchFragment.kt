package net.gearmaniacs.tournament.ui.fragment.nav

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.view.EmptyRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener
import net.gearmaniacs.tournament.ui.adapter.MatchAdapter
import net.gearmaniacs.tournament.ui.fragment.MatchEditDialog
import net.gearmaniacs.tournament.ui.fragment.TournamentFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class MatchFragment
    : TournamentFragment(R.layout.fragment_recycler_view), RecyclerViewItemListener {

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var adapter: MatchAdapter
    private var nextMatchId = 1

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        emptyView.setText(R.string.empty_tab_matches)

        adapter = MatchAdapter(this)

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

            setEmptyView(emptyView)
        }
        recyclerView.adapter = adapter

        activity.observe(viewModel.getMatchesLiveData()) {
            val matches = it ?: emptyList()

            adapter.submitList(matches)
            nextMatchId = (matches.maxBy { match -> match.id }?.id ?: 0) + 1
        }
    }

    override fun fabClickListener() {
        val activity = activity ?: return

        val dialog = MatchEditDialog.newInstance(nextMatchId)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun getFragmentTag() = fragmentTag

    override fun onClickListener(position: Int) {
        val activity = activity ?: return
        val match = adapter.getItem(position)

        val dialog = MatchEditDialog.newInstance(match)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun onLongClickListener(position: Int) {
        val activity = activity ?: return
        val key = adapter.getItem(position).key

        AlertDialog.Builder(activity)
            .setTitle(R.string.delete_match)
            .setMessage(R.string.delete_match_desc)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteMatch(key)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object : ICompanion {
        override val fragmentTag = "MatchFragment"

        override fun newInstance() =
            MatchFragment()
    }
}
