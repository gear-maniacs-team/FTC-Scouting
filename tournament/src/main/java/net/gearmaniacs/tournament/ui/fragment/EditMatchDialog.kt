package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.extensions.toIntOrElse
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.EditMatchDialogBinding
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class EditMatchDialog : DialogFragment() {

    private var _binding: EditMatchDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<TournamentViewModel>()
    private var transitionPlayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)

        transitionPlayed = savedInstanceState?.getBoolean(BUNDLE_TRANSITION_PLAYED) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BUNDLE_TRANSITION_PLAYED, transitionPlayed)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditMatchDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.bottomBar.setNavigationOnClickListener { dismiss() }
        binding.bottomBar.doOnPreDraw { bottom_bar ->
            binding.content.layoutContent.updatePadding(bottom = (bottom_bar.height * 1.6f).toInt())
        }

        lifecycleScope.launch {
            delay(50L)
            binding.fabDone.hide()
            delay(400L)
            binding.fabDone.show()
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

        binding.fabDone.setOnClickListener {
            // Parse Match data
            val redAlliance = Alliance(
                firstTeam = content.etRedFirstTeam.textString.toIntOrElse(),
                secondTeam = content.etRedSecondTeam.textString.toIntOrElse(),
                score = content.etRedScore.textString.toIntOrElse()
            )

            val blueAlliance = Alliance(
                firstTeam = content.etBlueFirstTeam.textString.toIntOrElse(),
                secondTeam = content.etBlueSecondTeam.textString.toIntOrElse(),
                score = content.etBlueScore.textString.toIntOrElse()
            )

            val parsedMatch = Match(
                match?.key.orEmpty(),
                id = content.etMatchNumber.textString.toIntOrElse(),
                redAlliance = redAlliance,
                blueAlliance = blueAlliance
            )

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

    companion object {
        private const val BUNDLE_TRANSITION_PLAYED = "bundle_transition_played"

        private const val ARG_NEXT_MATCH_ID = "arg_next_match_id"
        private const val ARG_MATCH = "arg_match"

        fun newInstance(nextMatchId: Int) = EditMatchDialog().apply {
            val bundle = Bundle()
            bundle.putInt(ARG_NEXT_MATCH_ID, nextMatchId)
            arguments = bundle
        }

        fun newInstance(match: Match) = EditMatchDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARG_MATCH, match)
            arguments = bundle
        }
    }
}
