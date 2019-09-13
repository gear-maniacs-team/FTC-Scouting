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
import net.gearmaniacs.tournament.ui.adapter.MatchAdapter
import net.gearmaniacs.tournament.utils.RecyclerViewItemListener
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class MatchFragment : TournamentFragment(R.layout.fragment_recycler_view),
    RecyclerViewItemListener {

    companion object {
        const val TAG = "MatchFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var adapter: MatchAdapter

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val fab = activity.fab
        view.empty_view.setText(R.string.empty_tab_matches)
        val recyclerView = view.recycler_view

        adapter = MatchAdapter(this)

        recyclerView.emptyView = view.empty_view
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

        activity.observeNonNull(viewModel.getMatchesLiveData()) {
            adapter.submitList(it)
        }
    }

    override fun fabClickListener() {
        val activity = activity ?: return

        val dialog = MatchEditDialog()
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun getFragmentTag() = TAG

    override fun onClickListener(position: Int) {
        val activity = activity ?: return
        val match = adapter.getItem(position)

        val dialog = MatchEditDialog.newInstance(match)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun onLongClickListener(position: Int) {
        val activity = activity ?: return
        val key = adapter.getItem(position).key ?: return

        AlertDialog.Builder(activity)
            .setTitle(R.string.delete_match)
            .setMessage(R.string.delete_match_desc)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteMatch(key)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
