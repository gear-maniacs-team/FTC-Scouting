package net.gearmaniacs.tournament.ui.fragment.nav

import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.view.EmptyRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.AnalyticsAdapter
import net.gearmaniacs.tournament.ui.fragment.TournamentFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class AnalyticsFragment : TournamentFragment(R.layout.fragment_recycler_view) {

    private val viewModel by activityViewModels<TournamentViewModel>()

    override fun onInflateView(view: View) {
        val activity = activity ?: return

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        emptyView.setText(R.string.empty_tab_analytics)

        val adapter = AnalyticsAdapter()

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

            setEmptyView(emptyView)
            setFabToHide(fab)
        }
        recyclerView.adapter = adapter

        activity.observeNonNull(viewModel.analyticsData) {
            adapter.submitList(it)
        }

        activity.observe(viewModel.getMatchesLiveData()) {
            if (it != null)
                viewModel.refreshAnalyticsData(false)
        }
    }

    override fun fabClickListener() {
        viewModel.refreshAnalyticsData()
    }

    override fun getFragmentTag() = fragmentTag

    companion object : ICompanion {
        override val fragmentTag = "AnalyticsFragment"

        override fun newInstance() = AnalyticsFragment()
    }
}
