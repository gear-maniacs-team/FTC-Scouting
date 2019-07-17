package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.core.extensions.lazyFast
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.core.model.User
import net.gearmaniacs.tournament.ui.activity.TournamentActivity
import net.gearmaniacs.tournament.ui.adapter.InfoAdapter
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class InfoFragment : TournamentsFragment() {

    companion object {
        const val TAG = "InfoFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private val adapter by lazyFast { InfoAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: return null

        activity.rv_main.adapter = adapter

        val user = activity.intent.getParcelableExtra<User>(TournamentActivity.ARG_USER) ?: return null

        activity.observeNonNull(viewModel.matchesData) {
            adapter.matchList = it.filter { match -> match.containsTeam(user.id) }
            adapter.notifyDataSetChanged()
        }

        return null
    }

    override fun fabClickListener() = Unit

    override fun getFragmentTag() = TAG
}
