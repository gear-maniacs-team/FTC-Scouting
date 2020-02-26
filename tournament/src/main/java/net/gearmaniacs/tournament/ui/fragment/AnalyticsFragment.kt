package net.gearmaniacs.tournament.ui.fragment

import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.view.EmptyRecyclerView
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

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        emptyView.setText(R.string.empty_tab_analytics)

        val adapter = AnalyticsAdapter()

        recyclerView.emptyView = emptyView
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

        viewModel.refreshAnalyticsData(activity.applicationContext)

        activity.observeNonNull(viewModel.analyticsData) {
            adapter.submitList(it)
        }
    }

    override fun fabClickListener() {
        context?.applicationContext?.let { viewModel.refreshAnalyticsData(it) }
    }

    override fun getFragmentTag() = TAG
}
