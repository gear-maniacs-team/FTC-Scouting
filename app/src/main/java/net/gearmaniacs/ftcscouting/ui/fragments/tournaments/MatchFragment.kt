package net.gearmaniacs.ftcscouting.ui.fragments.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.ui.adapter.MatchAdapter
import net.gearmaniacs.ftcscouting.utils.DataRecyclerViewListener
import net.gearmaniacs.ftcscouting.utils.architecture.observeNonNull
import net.gearmaniacs.ftcscouting.viewmodel.TournamentViewModel

class MatchFragment : TournamentsFragment(), DataRecyclerViewListener {

    companion object {
        const val TAG = "MatchFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var adapter: MatchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: return null
        val recyclerView = activity.rv_main

        adapter = MatchAdapter(recyclerView, this)
        recyclerView.adapter = adapter

        activity.observeNonNull(viewModel.matchesData) {
            adapter.submitList(it)
        }

        return null
    }

    override fun fabClickListener() {
        val activity = activity ?: return

        val dialog = MatchEditDialog()
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun getFragmentTag() = TAG

    override fun onEditItem(position: Int) {
        val activity = activity ?: return
        val match = adapter.getItem(position)

        val dialog = MatchEditDialog.newInstance(match)
        val transaction = activity.supportFragmentManager.beginTransaction()
        dialog.show(transaction, null)
    }

    override fun onDeleteItem(position: Int) {
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
