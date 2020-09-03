package net.gearmaniacs.tournament.ui.fragment.nav

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.core.utils.EmptyViewAdapter
import net.gearmaniacs.core.view.FabRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.adapter.InfoAdapter
import net.gearmaniacs.tournament.ui.fragment.AbstractTournamentFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class InfoFragment : AbstractTournamentFragment(R.layout.recycler_view_layout) {

    private val viewModel by activityViewModels<TournamentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        val recyclerView = view.findViewById<FabRecyclerView>(R.id.recycler_view)

        fab.hide()

        val emptyViewAdapter = EmptyViewAdapter()
        emptyViewAdapter.text = getString(R.string.empty_tab_info)
        val infoAdapter = InfoAdapter()

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = ConcatAdapter(emptyViewAdapter, infoAdapter)
        }

        val user = activity.intent.getParcelableExtra<UserData>(TournamentActivity.ARG_USER)

        if (!user.isNullOrEmpty()) {
            activity.observeNonNull(viewModel.getInfoLiveData(user)) {
                infoAdapter.submitList(it)

                emptyViewAdapter.isVisible = it.isEmpty()
            }
            emptyViewAdapter.text = getString(R.string.empty_tab_info)
        } else {
            emptyViewAdapter.text = getString(R.string.team_details_not_found)
        }
    }

    override fun fabClickListener() = Unit

    override fun getFragmentTag() = fragmentTag

    companion object : ICompanion {
        override val fragmentTag = "InfoFragment"

        override fun newInstance() = InfoFragment()
    }
}
