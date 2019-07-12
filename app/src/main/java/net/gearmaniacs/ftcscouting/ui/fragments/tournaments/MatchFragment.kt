package net.gearmaniacs.ftcscouting.ui.fragments.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.ui.adapter.MatchAdapter
import net.gearmaniacs.ftcscouting.ui.viewmodel.TournamentViewModel
import net.gearmaniacs.ftcscouting.utils.DataRecyclerListener
import net.gearmaniacs.ftcscouting.utils.architecture.getViewModel
import net.gearmaniacs.ftcscouting.utils.architecture.observe
import net.gearmaniacs.ftcscouting.utils.extensions.lazyFast

class MatchFragment : TournamentsFragment(), DataRecyclerListener {

    companion object {
        const val TAG = "MatchFragment"
    }

    private val viewModel by lazyFast { activity!!.getViewModel<TournamentViewModel>() }
    private lateinit var adapter: MatchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: return null
        val recyclerView = activity.recycler_view

        adapter = MatchAdapter(recyclerView, this)
        recyclerView.adapter = adapter

        activity.observe(viewModel.matchesData) {
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
                deleteMatch(key)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteMatch(key: String) {
        viewModel.currentUserReference
            .child("data")
            .child(viewModel.tournamentKey)
            .child("matches")
            .child(key)
            .removeValue()
    }
}
