package net.gearmaniacs.tournament.ui.fragment

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_tournament.*
import kotlinx.android.synthetic.main.fragment_recycler_view.view.*
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.AnalyticsAdapter
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class AnalyticsFragment : TournamentFragment(R.layout.fragment_recycler_view) {

    companion object {
        const val TAG = "AnalyticsFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val fab = activity.fab
        view.empty_view.setText(R.string.empty_tab_analytics)
        val recyclerView = view.recycler_view

        val adapter = AnalyticsAdapter()

        recyclerView.emptyView = view.empty_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }
        })

        viewModel.calculateOpr(activity.applicationContext)

        activity.observeNonNull(viewModel.analyticsData) {
            adapter.submitList(it)
        }
    }

    override fun fabClickListener() {
        context?.applicationContext?.let { viewModel.calculateOpr(it) }
    }

    override fun getFragmentTag() = TAG
}
