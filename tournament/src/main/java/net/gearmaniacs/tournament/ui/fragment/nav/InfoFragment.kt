package net.gearmaniacs.tournament.ui.fragment.nav

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.core.utils.EmptyViewAdapter
import net.gearmaniacs.core.view.FabRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.adapter.InfoAdapter
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class InfoFragment : Fragment(R.layout.recycler_view_layout) {

    private val viewModel by activityViewModels<TournamentViewModel>()
    private var fab: FloatingActionButton? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()

        fab = activity.findViewById(R.id.fab)
        val recyclerView = view.findViewById<FabRecyclerView>(R.id.recycler_view)

        val emptyViewAdapter = EmptyViewAdapter()
        emptyViewAdapter.text = getString(R.string.empty_tab_info)
        val infoAdapter = InfoAdapter()

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = ConcatAdapter(emptyViewAdapter, infoAdapter)
        }

        val user = activity.intent.getParcelableExtra<UserTeam>(TournamentActivity.ARG_USER)

        if (!user.isNullOrEmpty()) {
            viewLifecycleOwner.observeNonNull(viewModel.getInfoLiveData(user)) {
                infoAdapter.submitList(it)

                emptyViewAdapter.isVisible = it.isEmpty()
            }
            emptyViewAdapter.text = getString(R.string.empty_tab_info)
        } else {
            emptyViewAdapter.text = getString(R.string.team_details_not_found)
        }
    }

    override fun onStart() {
        super.onStart()
        fab?.let {
            if (it.isOrWillBeShown) {
                it.hide()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fab = null
    }
}
