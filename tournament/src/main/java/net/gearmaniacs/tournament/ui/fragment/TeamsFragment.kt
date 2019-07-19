package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.TeamAdapter
import net.gearmaniacs.tournament.utils.DataRecyclerViewListener
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class TeamsFragment : TournamentsFragment(), DataRecyclerViewListener {

    companion object {
        const val TAG = "TeamsFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var adapter: TeamAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = activity ?: return
        val recyclerView = activity.rv_main

        if (!this::adapter.isInitialized)
            adapter = TeamAdapter(recyclerView, this)
        recyclerView.adapter = adapter

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