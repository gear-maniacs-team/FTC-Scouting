package net.gearmaniacs.tournament.ui.fragment.nav

import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.view.EmptyRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.AnalyticsAdapter
import net.gearmaniacs.tournament.ui.fragment.AbstractTournamentFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class AnalyticsFragment : AbstractTournamentFragment(R.layout.fragment_recycler_view) {

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var emptyView: TextView

    private var teamsList = emptyList<Team>()
    private var matchesList = emptyList<Match>()
    private var observedDataChanged = true

    override fun onInflateView(view: View) {
        val activity = requireActivity()

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        emptyView = view.findViewById(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        fab.hide()
        emptyView.setText(R.string.empty_tab_analytics)

        val adapter = AnalyticsAdapter()

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

            setEmptyView(emptyView)
        }
        recyclerView.adapter = adapter

        activity.observe(viewModel.getTeamsLiveData()) {
            teamsList = it ?: emptyList()
            if (it != null)
                observedDataChanged = true
        }

        activity.observe(viewModel.getMatchesLiveData()) {
            matchesList = it ?: emptyList()
            if (it != null)
                observedDataChanged = true
        }

        activity.observeNonNull(viewModel.getAnalyticsLiveData()) {
            adapter.submitList(it)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden && observedDataChanged) {
            observedDataChanged = false
            refreshData()
        }
    }

    override fun fabClickListener() = Unit

    override fun getFragmentTag() = fragmentTag

    private fun refreshData() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            // TODO: Refactor getMatchesLiveData() observable into Response<List<TeamPower>, String>
            val response = viewModel.refreshAnalyticsData(teamsList, matchesList)
            emptyView.text = response
        }
    }

    companion object : ICompanion {
        override val fragmentTag = "AnalyticsFragment"

        override fun newInstance() = AnalyticsFragment()
    }
}
