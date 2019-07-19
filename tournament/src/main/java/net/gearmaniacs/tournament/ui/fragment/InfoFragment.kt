package net.gearmaniacs.tournament.ui.fragment

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_recycler_view.view.*
import net.gearmaniacs.core.extensions.lazyFast
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.model.User
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.adapter.InfoAdapter
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class InfoFragment : TournamentsFragment(R.layout.fragment_recycler_view) {

    companion object {
        const val TAG = "InfoFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private val adapter by lazyFast { InfoAdapter() }

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val recyclerView = view.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        val user = activity.intent.getParcelableExtra<User>(TournamentActivity.ARG_USER) ?: return

        activity.observeNonNull(viewModel.matchesData) {
            adapter.matchList = it.filter { match -> match.containsTeam(user.id) }
            adapter.notifyDataSetChanged()
        }
    }

    override fun fabClickListener() = Unit

    override fun getFragmentTag() = TAG
}
