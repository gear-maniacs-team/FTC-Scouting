package net.gearmaniacs.ftcscouting.ui.fragments.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.ftcscouting.ui.adapter.AnalyticsAdapter
import net.gearmaniacs.ftcscouting.utils.architecture.observeNonNull
import net.gearmaniacs.ftcscouting.viewmodel.TournamentViewModel

class AnalyticsFragment : TournamentsFragment() {

    companion object {
        const val TAG = "AnalyticsFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: return null
        val adapter = AnalyticsAdapter()

        activity.rv_main.adapter = adapter

        activity.observeNonNull(viewModel.analyticsData) {
            adapter.submitList(it)
        }

        return null
    }

    override fun fabClickListener() {
        context?.applicationContext?.let { viewModel.calculateOpr(it) }
    }

    override fun getFragmentTag() = TAG
}
