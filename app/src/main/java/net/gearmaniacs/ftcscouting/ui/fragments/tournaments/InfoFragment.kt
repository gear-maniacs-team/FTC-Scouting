package net.gearmaniacs.ftcscouting.ui.fragments.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.core.model.User
import net.gearmaniacs.ftcscouting.ui.activities.TournamentActivity
import net.gearmaniacs.ftcscouting.ui.adapter.InfoAdapter
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.ftcscouting.viewmodel.TournamentViewModel

class InfoFragment : TournamentsFragment() {

    companion object {
        const val TAG = "InfoFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: return null

        val adapter = InfoAdapter()
        activity.rv_main.adapter = adapter

        val user = activity.intent.getParcelableExtra<User>(TournamentActivity.ARG_USER)

        activity.observeNonNull(viewModel.matchesData) {
            adapter.matchList = it.filter { match -> match.containsTeam(user.id) }
            adapter.notifyDataSetChanged()
        }

        return null
    }

    override fun fabClickListener() = Unit

    override fun getFragmentTag() = TAG
}
