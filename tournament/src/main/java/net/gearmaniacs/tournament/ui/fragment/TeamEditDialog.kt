package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.isInvisible
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.model.AutonomousData
import net.gearmaniacs.core.model.EndGameData
import net.gearmaniacs.core.model.PreferredZone
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeleOpData
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.DialogEditTeamBinding
import net.gearmaniacs.tournament.databinding.DialogEditTeamContentBinding
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

internal class TeamEditDialog : DialogFragment() {

    companion object {
        private const val ARG_TEAM = "team"

        fun newInstance() = TeamEditDialog()

        fun newInstance(team: Team) = TeamEditDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARG_TEAM, team)
            arguments = bundle
        }
    }

    private class DataChangeListener(
        private val listener: () -> Unit
    ) : RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            listener()
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            listener()
        }

        override fun afterTextChanged(s: Editable?) {
            listener()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }

    private var _binding: DialogEditTeamBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<TournamentViewModel>()
    private var transitionPlayed = false

    private var autonomousScore = 0
    private var teleOpScore = 0
    private var endGameScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogEditTeamBinding.inflate(inflater, container, false)
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
        val team = arguments?.getParcelable<Team>(ARG_TEAM)
        val content = binding.content

        content.cbCapPlaced.setOnCheckedChangeListener { _, isChecked ->
            content.layoutCapLevel.isInvisible = !isChecked
            updateEndGameScore()
            updateTotalScore()
        }

        binding.fabEditTeamDone.setOnClickListener {
            // Parse Team data
            val autonomousData = parseAutonomousData(content)
            val teleOpData = parseTeleOpData(content)
            val endGameData = parseEndGameData()

            val preferredZone = when {
                content.rbZoneBuilding.isChecked -> PreferredZone.BUILDING
                content.rbZoneLoading.isChecked -> PreferredZone.LOADING
                else -> PreferredZone.NONE
            }

            val notesText = content.etNotes.getTextString()
            val parsedTeam = Team(
                id = content.etTeamNumber.getTextString().toIntOrDefault(),
                name = content.etTeamName.getTextString(),
                autonomousData = autonomousData.takeIf { it.isNotEmpty },
                teleOpData = teleOpData.takeIf { it.isNotEmpty },
                endGameData = endGameData.takeIf { it.isNotEmpty },
                preferredZone = preferredZone,
                notes = notesText.takeIf { it.isNotBlank() }
            )

            parsedTeam.key = team?.key
            viewModel.updateTeam(parsedTeam)

            dismiss()
        }

        setupListeners()

        if (team != null)
            restoreTeamData(team)

        updateAutonomousScore(autonomousScore)
        updateTeleOpScore(teleOpScore)
        updateEndGameScore(endGameScore)
        updateTotalScore()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setupListeners() {
        val autonomousListener = DataChangeListener {
            updateAutonomousScore()
            updateTotalScore()
        }
        val teleOpListener = DataChangeListener {
            updateTeleOpScore()
            updateTotalScore()
        }
        val endGameListener = DataChangeListener {
            updateEndGameScore()
            updateTotalScore()
        }

        with(binding.content) {
            cbRepositionFoundation.setOnCheckedChangeListener(autonomousListener)
            cbNavigated.setOnCheckedChangeListener(autonomousListener)
            etAutoDeliveredSkystones.addTextChangedListener(autonomousListener)
            etAutoDeliveredStones.addTextChangedListener(autonomousListener)
            etAutoPlacedStones.addTextChangedListener(autonomousListener)
            etDeliveredStones.addTextChangedListener(teleOpListener)
            etPlacedStones.addTextChangedListener(teleOpListener)
            etSkyscraperHeight.addTextChangedListener(teleOpListener)
            cbMoveFoundation.setOnCheckedChangeListener(endGameListener)
            cbParking.setOnCheckedChangeListener(endGameListener)
            etCapLevel.addTextChangedListener(endGameListener)
        }
    }

    private fun restoreTeamData(team: Team) {
        with(binding.content) {
            etTeamNumber.setText(team.id.toString())
            etTeamName.setText(team.name)

            team.autonomousData?.let {
                cbRepositionFoundation.isChecked = it.repositionFoundation
                cbNavigated.isChecked = it.navigated
                etAutoDeliveredSkystones.setText(it.deliveredSkystones.toString())
                etAutoDeliveredStones.setText(it.deliveredStones.toString())
                etAutoPlacedStones.setText(it.placedStones.toString())

                autonomousScore = it.calculateScore()
            }

            team.teleOpData?.let {
                etDeliveredStones.setText(it.deliveredStones.toString())
                etPlacedStones.setText(it.placedStones.toString())
                etSkyscraperHeight.setText(it.skyscraperHeight.toString())

                teleOpScore = it.calculateScore()
            }

            team.endGameData?.let {
                cbMoveFoundation.isChecked = it.moveFoundation
                cbParking.isChecked = it.parked

                if (it.capLevel >= 0) {
                    cbCapPlaced.isChecked = true
                    etCapLevel.setText(it.capLevel.toString())
                }

                endGameScore = it.calculateScore()
            }

            when (team.preferredZone) {
                PreferredZone.BUILDING -> rbZoneBuilding.isChecked = true
                PreferredZone.LOADING -> rbZoneLoading.isChecked = true
            }

            etNotes.setText(team.notes)
        }
    }

    private fun parseAutonomousData(content: DialogEditTeamContentBinding) = AutonomousData(
        content.cbRepositionFoundation.isChecked,
        content.cbNavigated.isChecked,
        content.etAutoDeliveredSkystones.getTextString().toIntOrDefault(),
        content.etAutoDeliveredStones.getTextString().toIntOrDefault(),
        content.etAutoPlacedStones.getTextString().toIntOrDefault()
    )

    private fun parseTeleOpData(content: DialogEditTeamContentBinding) = TeleOpData(
        content.etAutoDeliveredStones.getTextString().toIntOrDefault(),
        content.etPlacedStones.getTextString().toIntOrDefault(),
        content.etSkyscraperHeight.getTextString().toIntOrDefault()
    )

    private fun parseEndGameData(): EndGameData {
        val content = binding.content
        val capLevel =
            if (content.cbCapPlaced.isChecked) content.etCapLevel.getTextString().toIntOrDefault() else -1

        return EndGameData(
            content.cbMoveFoundation.isChecked,
            content.cbParking.isChecked,
            capLevel
        )
    }

    private fun updateAutonomousScore(score: Int = -1) {
        val newScore =
            if (score == -1) parseAutonomousData(binding.content).calculateScore() else score

        autonomousScore = newScore
        binding.content.tvAutonomousScore.text = getString(R.string.autonomous_score, newScore)
    }

    private fun updateTeleOpScore(score: Int = -1) {
        val newScore =
            if (score == -1) parseTeleOpData(binding.content).calculateScore() else score

        teleOpScore = newScore
        binding.content.tvTeleopScore.text = getString(R.string.teleop_score, newScore)
    }

    private fun updateEndGameScore(score: Int = -1) {
        val newScore = if (score == -1) parseEndGameData().calculateScore() else score

        endGameScore = newScore
        binding.content.tvEndgameScore.text = getString(R.string.endgame_score, newScore)
    }

    private fun updateTotalScore() {
        val totalScore = autonomousScore + teleOpScore + endGameScore

        binding.content.tvTotalScore.text = getString(R.string.total_score, totalScore)
    }
}
