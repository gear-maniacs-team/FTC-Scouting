package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_tournament.*
import net.gearmaniacs.core.extensions.lazyFast
import net.gearmaniacs.core.extensions.observeNonNull
import net.gearmaniacs.tournament.ui.adapter.AnalyticsAdapter
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class AnalyticsFragment : TournamentsFragment() {

    companion object {
        const val TAG = "AnalyticsFragment"
    }

    private val viewModel by activityViewModels<TournamentViewModel>()
    private val adapter by lazyFast { AnalyticsAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = activity ?: return null

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
