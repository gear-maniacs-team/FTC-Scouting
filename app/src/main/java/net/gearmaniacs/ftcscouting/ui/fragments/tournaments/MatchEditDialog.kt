package net.gearmaniacs.ftcscouting.ui.fragments.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_edit_match.view.*
import kotlinx.android.synthetic.main.dialog_edit_match_content.view.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.data.Alliance
import net.gearmaniacs.ftcscouting.data.Match
import net.gearmaniacs.ftcscouting.ui.viewmodel.TournamentViewModel
import net.gearmaniacs.ftcscouting.utils.architecture.getViewModel
import net.gearmaniacs.ftcscouting.utils.extensions.getTextOrEmpty
import net.gearmaniacs.ftcscouting.utils.extensions.lazyFast
import net.gearmaniacs.ftcscouting.utils.extensions.toIntOrDefault

class MatchEditDialog : DialogFragment() {

    companion object {
        private const val ARG_MATCH = "match"

        fun newInstance(match: Match) = MatchEditDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARG_MATCH, match)
            arguments = bundle
        }
    }

    private val viewModel by lazyFast { activity!!.getViewModel<TournamentViewModel>() }
    private var transitionPlayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.dialog_edit_match, container, false)

        view.toolbar.setNavigationIcon(R.drawable.ic_close)
        view.toolbar.setNavigationOnClickListener { dismiss() }
        view.toolbar.title = null

        view.toolbar.doOnPreDraw { toolbar ->
            view.layout_content.updatePadding(bottom = (toolbar.height * 1.6f).toInt())
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        if (!transitionPlayed) {
            transitionPlayed = true
            dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val match = arguments?.getParcelable<Match>(ARG_MATCH)

        view.fab_edit_match_done.setOnClickListener {
            // Parse Match data
            val redAlliance = Alliance(
                firstTeam = view.et_alliance_red_first.getTextOrEmpty().toIntOrDefault(),
                secondTeam = view.et_alliance_red_second.getTextOrEmpty().toIntOrDefault(),
                score = view.et_alliance_red_score.getTextOrEmpty().toIntOrDefault()
            )

            val blueAlliance = Alliance(
                firstTeam = view.et_alliance_blue_first.getTextOrEmpty().toIntOrDefault(),
                secondTeam = view.et_alliance_blue_second.getTextOrEmpty().toIntOrDefault(),
                score = view.et_alliance_blue_score.getTextOrEmpty().toIntOrDefault()
            )

            val parsedMatch = Match(
                id = view.et_match_number.getTextOrEmpty().toIntOrDefault(),
                redAlliance = redAlliance,
                blueAlliance = blueAlliance
            )

            val matchesRef = viewModel.currentUserReference
                .child("data")
                .child(viewModel.tournamentKey)
                .child("matches")

            if (match == null) {
                matchesRef.push().setValue(parsedMatch)
            } else {
                matchesRef.child(match.key!!).setValue(parsedMatch)
            }

            dismiss()
        }

        match ?: return

        view.apply {
            et_match_number.setText(match.id.toString())

            match.redAlliance.let {
                et_alliance_red_first.setText(it.firstTeam.toString())
                et_alliance_red_second.setText(it.secondTeam.toString())
            }

            match.blueAlliance.let {
                et_alliance_blue_first.setText(it.firstTeam.toString())
                et_alliance_blue_second.setText(it.secondTeam.toString())
            }

            et_alliance_red_score.setText(match.redAlliance.score.toString())
            et_alliance_blue_score.setText(match.blueAlliance.score.toString())
        }
    }
}
