package net.gearmaniacs.tournament.ui.fragment.nav

import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.view.EmptyRecyclerView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.adapter.InfoAdapter
import net.gearmaniacs.tournament.ui.fragment.TournamentFragment
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class InfoFragment : TournamentFragment(R.layout.fragment_recycler_view) {

    private val viewModel by activityViewModels<TournamentViewModel>()

    override fun onInflateView(view: View) {
        val activity = requireActivity()

        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val recyclerView = view.findViewById<EmptyRecyclerView>(R.id.recycler_view)

        fab.hide()
        emptyView.setText(R.string.empty_tab_info)

        val adapter = InfoAdapter()

        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)

            setEmptyView(emptyView)
        }
        recyclerView.adapter = adapter

        val user = activity.intent.getParcelableExtra<UserData>(TournamentActivity.ARG_USER)

        if (user != null) {
            activity.observeNonNull(viewModel.getInfoLiveData(user)) {
                adapter.submitList(it)
            }
        } else {
            emptyView.setText(R.string.team_info_not_found)
        }
    }

    override fun fabClickListener() = Unit

    override fun getFragmentTag() = fragmentTag

    companion object : ICompanion {
        override val fragmentTag = "InfoFragment"

        override fun newInstance() = InfoFragment()
    }
}
