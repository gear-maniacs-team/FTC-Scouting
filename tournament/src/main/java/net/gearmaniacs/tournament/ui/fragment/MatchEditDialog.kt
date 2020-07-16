package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.DialogEditMatchBinding
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class MatchEditDialog : DialogFragment() {

    companion object {
        private const val ARG_NEXT_MATCH_ID = "next_match_id"
        private const val ARG_MATCH = "match"

        fun newInstance(nextMatchId: Int) = MatchEditDialog().apply {
            val bundle = Bundle()
            bundle.putInt(ARG_NEXT_MATCH_ID, nextMatchId)
            arguments = bundle
        }

        fun newInstance(match: Match) = MatchEditDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARG_MATCH, match)
            arguments = bundle
        }
    }

    private var _binding: DialogEditMatchBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<TournamentViewModel>()
    private var transitionPlayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditMatchBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.bottomBar.setNavigationIcon(R.drawable.ic_close)
        binding.bottomBar.setNavigationOnClickListener { dismiss() }

        binding.bottomBar.doOnPreDraw { bottom_bar ->
            binding.content.layoutContent.updatePadding(bottom = (bottom_bar.height * 1.6f).toInt())
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
        val nextMatchId = arguments?.getInt(ARG_NEXT_MATCH_ID, 1) ?: 1
        val match = arguments?.getParcelable<Match>(ARG_MATCH)
        val content = binding.content

        content.etMatchNumber.setText(nextMatchId.toString())

        binding.fabEditMatchDone.setOnClickListener {
            // Parse Match data
            val redAlliance = Alliance(
                firstTeam = content.etRedFirstTeam.getTextString().toIntOrDefault(),
                secondTeam = content.etRedSecondTeam.getTextString().toIntOrDefault(),
                score = content.etRedScore.getTextString().toIntOrDefault()
            )

            val blueAlliance = Alliance(
                firstTeam = content.etBlueFirstTeam.getTextString().toIntOrDefault(),
                secondTeam = content.etBlueSecondTeam.getTextString().toIntOrDefault(),
                score = content.etBlueScore.getTextString().toIntOrDefault()
            )

            val parsedMatch = Match(
                id = content.etMatchNumber.getTextString().toIntOrDefault(),
                redAlliance = redAlliance,
                blueAlliance = blueAlliance
            )

            parsedMatch.key = match?.key
            viewModel.updateMatch(parsedMatch)

            dismiss()
        }

        match ?: return

        with(content) {
            etMatchNumber.setText(match.id.toString())

            match.redAlliance.let {
                etRedFirstTeam.setText(it.firstTeam.toString())
                etRedSecondTeam.setText(it.secondTeam.toString())
            }

            match.blueAlliance.let {
                etBlueFirstTeam.setText(it.firstTeam.toString())
                etBlueSecondTeam.setText(it.secondTeam.toString())
            }

            etRedScore.setText(match.redAlliance.score.toString())
            etBlueScore.setText(match.blueAlliance.score.toString())
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
