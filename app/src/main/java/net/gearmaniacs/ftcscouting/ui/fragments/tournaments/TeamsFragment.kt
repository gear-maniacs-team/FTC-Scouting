package net.gearmaniacs.ftcscouting.ui.fragments.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.ui.adapter.TeamAdapter
import net.gearmaniacs.ftcscouting.utils.DataRecyclerViewListener
import net.gearmaniacs.ftcscouting.utils.architecture.observeNonNull
import net.gearmaniacs.ftcscouting.viewmodel.TournamentViewModel

class TeamsFragment : TournamentsFragment(), DataRecyclerViewListener {

    companion object {
        const val TAG = "TeamsFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var adapter: TeamAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: return null
        val recyclerView = activity.rv_main

        adapter = TeamAdapter(recyclerView, this)
        recyclerView.adapter = adapter

        activity.observeNonNull(viewModel.teamsData) {
            adapter.submitList(it)
        }

        return null
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
