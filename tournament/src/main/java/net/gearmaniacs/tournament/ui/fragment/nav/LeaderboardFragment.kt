package net.gearmaniacs.tournament.ui.fragment.nav

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
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
import net.gearmaniacs.core.utils.EmptyViewAdapter
import net.gearmaniacs.core.view.FabRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.adapter.LeaderboardAdapter
import net.gearmaniacs.tournament.ui.fragment.AbstractTournamentFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class LeaderboardFragment : AbstractTournamentFragment(R.layout.recycler_view_layout) {

    private val viewModel by activityViewModels<TournamentViewModel>()
    private lateinit var emptyViewAdapter: EmptyViewAdapter

    private var teamsList = emptyList<Team>()
    private var matchesList = emptyList<Match>()
    private var observedDataChanged = true

    override fun onInflateView(view: View) {
        val activity = requireActivity()

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        val recyclerView = view.findViewById<FabRecyclerView>(R.id.recycler_view)

        fab.hide()

        emptyViewAdapter = EmptyViewAdapter()
        emptyViewAdapter.text = getString(R.string.empty_tab_leaderboard)
        val leaderboardAdapter = LeaderboardAdapter()

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))
            adapter = ConcatAdapter(emptyViewAdapter, leaderboardAdapter)
        }

        activity.observe(viewModel.getTeamsLiveData()) {
            if (it != null) {
                teamsList = it
                observedDataChanged = true
            }
        }

        activity.observe(viewModel.getMatchesLiveData()) {
            if (it != null) {
                matchesList = it
                observedDataChanged = true
            }
        }

        activity.observeNonNull(viewModel.getLeaderboardLiveData()) {
            leaderboardAdapter.submitList(it)

            emptyViewAdapter.isVisible = it.isEmpty()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden && observedDataChanged) {
            // Only recompute the Leaderboard data when needed
            observedDataChanged = false
            refreshData()
        }
    }

    override fun fabClickListener() = Unit

    override fun getFragmentTag() = fragmentTag

    private fun refreshData() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            // TODO: Refactor getMatchesLiveData() observable into Response<List<TeamPower>, String>
            val response = viewModel.refreshLeaderboardData(teamsList, matchesList)
            emptyViewAdapter.text = response
        }
    }

    companion object : ICompanion {
        override val fragmentTag = LeaderboardFragment::class.simpleName!!

        override fun newInstance() = LeaderboardFragment()
    }
}