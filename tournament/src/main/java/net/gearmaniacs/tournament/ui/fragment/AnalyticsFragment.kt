package net.gearmaniacs.tournament.ui.fragment

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_recycler_view.view.*
import net.gearmaniacs.core.extensions.lazyFast
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.AnalyticsAdapter
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class AnalyticsFragment : TournamentsFragment(R.layout.fragment_recycler_view) {

    companion object {
        const val TAG = "AnalyticsFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private val adapter by lazyFast { AnalyticsAdapter() }

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val recyclerView = view.recycler_view

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        activity.observeNonNull(viewModel.analyticsData) {
            adapter.submitList(it)
        }
    }

    override fun fabClickListener() {
        context?.applicationContext?.let { viewModel.calculateOpr(it) }
    }

    override fun getFragmentTag() = TAG
}
