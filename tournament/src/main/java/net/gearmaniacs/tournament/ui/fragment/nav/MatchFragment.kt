package net.gearmaniacs.tournament.ui.fragment.nav

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.view.EmptyRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener
import net.gearmaniacs.tournament.ui.adapter.MatchAdapter
import net.gearmaniacs.tournament.ui.fragment.AbstractTournamentFragment
import net.gearmaniacs.tournament.ui.fragment.EditMatchDialog
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class MatchFragment
    : AbstractTournamentFragment(R.layout.empty_recycler_view_layout), RecyclerViewItemListener<Match> {

    private val viewModel by activityViewModels<TournamentViewModel>()
    private var nextMatchId = 1

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        emptyView.setText(R.string.empty_tab_matches)

        val adapter = MatchAdapter(this)

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

            setEmptyView(emptyView)
            setFabToHideOnScroll(fab)
        }
        recyclerView.adapter = adapter

        activity.observe(viewModel.getMatchesLiveData()) { matches ->
            if (matches != null) {
                adapter.submitList(matches)
                nextMatchId = (matches.maxByOrNull { it.id }?.id ?: 0) + 1
            }
        }
    }

    override fun fabClickListener() {
        val activity = activity ?: return

        val dialog = EditMatchDialog.newInstance(nextMatchId)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun getFragmentTag() = fragmentTag

    override fun onClickListener(item: Match) {
        val activity = activity ?: return

        val dialog = EditMatchDialog.newInstance(item)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun onLongClickListener(item: Match) {
        val activity = activity ?: return
        val key = item.key

        val message =
            if (Firebase.isLoggedIn) R.string.delete_match_desc else R.string.delete_match_desc_offline

        AlertDialog.Builder(activity)
            .setTitle(R.string.delete_match)
            .setMessage(message)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteMatch(key)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object : ICompanion {
        override val fragmentTag = "MatchFragment"

        override fun newInstance() = MatchFragment()
    }
}
