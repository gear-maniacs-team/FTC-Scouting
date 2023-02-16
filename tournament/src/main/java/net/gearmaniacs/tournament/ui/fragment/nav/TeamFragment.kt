package net.gearmaniacs.tournament.ui.fragment.nav

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.getCurrentNavigationFragment
import net.gearmaniacs.core.extensions.observe
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.core.utils.EmptyViewAdapter
import net.gearmaniacs.core.view.FabRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener
import net.gearmaniacs.tournament.ui.adapter.TeamAdapter
import net.gearmaniacs.tournament.ui.adapter.TeamSearchAdapter
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class TeamFragment : Fragment(R.layout.recycler_view_layout),
    RecyclerViewItemListener<Team> {

    private val viewModel by activityViewModels<TournamentViewModel>()

    private lateinit var searchAdapter: TeamSearchAdapter
    private var lastQuery: TeamSearchAdapter.Query? = null
    private var fab: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lastQuery = savedInstanceState?.getParcelable(BUNDLE_QUERY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()

        fab = activity.findViewById(R.id.fab)
        val recyclerView = view.findViewById<FabRecyclerView>(R.id.recycler_view)

        searchAdapter = TeamSearchAdapter {
            lastQuery = it
            viewModel.queryTeams(it)
        }
        lastQuery?.let { searchAdapter.setQuery(it) }

        val emptyViewAdapter = EmptyViewAdapter()
        emptyViewAdapter.text = getString(R.string.empty_tab_teams)

        val teamAdapter = TeamAdapter(this)

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))
            adapter = ConcatAdapter(searchAdapter, emptyViewAdapter, teamAdapter)

            setFabToHideOnScroll(fab!!)
        }

        fab!!.setOnClickListener {
            openEditDialog(null)
        }

        viewLifecycleOwner.observe(viewModel.getTeamsFilteredLiveData()) {
            if (it != null) {
                val query = lastQuery
                teamAdapter.submitList(it)

                val emptyViewVisible = it.isEmpty() && (query == null || query.isEmpty())
                emptyViewAdapter.isVisible = emptyViewVisible
                searchAdapter.isVisible = !emptyViewVisible
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fab?.let {
            if (it.isOrWillBeHidden) {
                it.show()
            }
        }
    }

    override fun onDestroy() {
        fab = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_QUERY, searchAdapter.getQuery())
    }

    override fun onClickListener(item: Team) {
        openEditDialog(item)
    }

    private fun openEditDialog(team: Team?) {
        val currentNavigationFragment =
            requireActivity().getCurrentNavigationFragment(R.id.nav_host_fragment)

        currentNavigationFragment?.also {
            exitTransition = MaterialElevationScale(false).apply {
                duration = 500L
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = 500L
            }
        }

        findNavController().navigate(TeamFragmentDirections.editTeam(team))
    }

    override fun onLongClickListener(item: Team) {
        val key = item.key

        val message =
            if (Firebase.isLoggedIn) R.string.delete_team_desc else R.string.delete_team_desc_offline

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_team)
            .setMessage(message)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteTeam(key)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val BUNDLE_QUERY = "query"
    }
}
