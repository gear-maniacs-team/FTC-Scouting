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
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButtonToggleGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.extensions.toIntOrElse
import net.gearmaniacs.core.model.enums.ColorMarker
import net.gearmaniacs.core.model.enums.PreferredZone
import net.gearmaniacs.core.model.enums.WobbleDeliveryZone
import net.gearmaniacs.core.model.team.AutonomousPeriod
import net.gearmaniacs.core.model.team.ControlledPeriod
import net.gearmaniacs.core.model.team.EndGamePeriod
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.core.view.CounterView
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.EditTeamContentDialogBinding
import net.gearmaniacs.tournament.databinding.EditTeamDialogBinding
import net.gearmaniacs.tournament.viewmodel.TournamentViewModel

@AndroidEntryPoint
internal class EditTeamDialog : DialogFragment() {

    companion object {
        const val TAG = "TeamEditDialog"

        private const val ARG_TEAM = "team"

        fun newInstance() = EditTeamDialog()

        fun newInstance(team: Team) = EditTeamDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARG_TEAM, team)
            arguments = bundle
        }
    }

    private class DataChangeListener(
        private val listener: () -> Unit
    ) : RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener,
        TextWatcher,
        MaterialButtonToggleGroup.OnButtonCheckedListener,
        CounterView.CounterChangeListener {

        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) = listener()

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = listener()

        override fun afterTextChanged(s: Editable?) = listener()

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun onIncrement(count: Int) = listener()

        override fun onDecrement(count: Int) = listener()

        override fun onButtonChecked(
            group: MaterialButtonToggleGroup?,
            checkedId: Int,
            isChecked: Boolean
        ) = listener()
    }

    private var _binding: EditTeamDialogBinding? = null
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
        _binding = EditTeamDialogBinding.inflate(inflater, container, false)

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val team = arguments?.getParcelable<Team>(ARG_TEAM)
        val content = binding.content

        binding.fabDone.setOnClickListener {
            // Parse Team data
            val autonomousPeriod = parseAutonomousPeriod(content)
            val controlledPeriod = parseControlledPeriod(content)
            val endGamePeriod = parseEndGameData(content)

            val preferredZone = when {
                content.rbZoneBuilding.isChecked -> PreferredZone.BUILDING
                content.rbZoneLoading.isChecked -> PreferredZone.LOADING
                else -> PreferredZone.NONE
            }

            val notesText = content.etNotes.textString
            val parsedTeam = Team(
                key = team?.key.orEmpty(),
                number = content.etTeamNumber.textString.toIntOrElse(),
                name = content.etTeamName.textString,
                autonomousPeriod = autonomousPeriod.takeIf { it.isNotEmpty() },
                controlledPeriod = controlledPeriod.takeIf { it.isNotEmpty() },
                endGamePeriod = endGamePeriod.takeIf { it.isNotEmpty() },
                colorMarker = parseColorMarker(),
                preferredZone = preferredZone,
                notes = notesText.takeIf { it.isNotBlank() }
            )

            viewModel.updateTeam(parsedTeam)

            dismiss()
        }

        setupListeners()

        if (team != null)
            restoreTeamData(team)

        updateAutonomousScore(autonomousScore)
        updateControlledScore(teleOpScore)
        updateEndGameScore(endGameScore)
        updateTotalScore()
    }

    override fun onStart() {
        super.onStart()

        if (!transitionPlayed) {
            transitionPlayed = true
            dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
        }
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
            updateControlledScore()
            updateTotalScore()
        }
        val endGameListener = DataChangeListener {
            updateEndGameScore()
            updateTotalScore()
        }

        with(binding.content) {
            swAutoWobbleDelivery.setOnCheckedChangeListener(autonomousListener)
            ctAutoLowGoal.changeListener = autonomousListener
            ctAutoMidGoal.changeListener = autonomousListener
            ctAutoHighGoal.changeListener = autonomousListener
            ctAutoPowerShot.changeListener = autonomousListener
            swAutoParking.setOnCheckedChangeListener(autonomousListener)
            ctLowGoal.changeListener = teleOpListener
            ctMidGoal.changeListener = teleOpListener
            ctHighGoal.changeListener = teleOpListener
            ctPowerShot.changeListener = endGameListener
            ctPowerShot.changeListener = endGameListener
            ctWobbleRings.changeListener = endGameListener
            toggleWobbleDelivery.addOnButtonCheckedListener(endGameListener)
        }
    }

    private fun restoreTeamData(team: Team) {
        with(binding.content) {
            etTeamNumber.setText(team.number.toString())
            etTeamName.setText(team.name)

            team.autonomousPeriod?.let {
                swAutoWobbleDelivery.isChecked = it.wobbleDelivery
                ctAutoLowGoal.counter = it.lowGoal
                ctAutoMidGoal.counter = it.midGoal
                ctAutoHighGoal.counter = it.highGoal
                ctAutoPowerShot.counter = it.powerShot
                swAutoParking.isChecked = it.parked

                autonomousScore = it.score()
            }

            team.controlledPeriod?.let {
                ctLowGoal.counter = it.lowGoal
                ctMidGoal.counter = it.midGoal
                ctHighGoal.counter = it.highGoal

                teleOpScore = it.score()
            }

            team.endGamePeriod?.let {
                ctPowerShot.counter = it.powerShot
                ctWobbleRings.counter = it.wobbleRings
                when (it.wobbleDeliveryZone) {
                    WobbleDeliveryZone.NONE -> rbWobbleDeliveryNone.isChecked = true
                    WobbleDeliveryZone.START_LINE -> rbWobbleDeliveryStartLine.isChecked = true
                    WobbleDeliveryZone.DEAD_ZONE -> rbWobbleDeliveryDeadZone.isChecked = true
                }

                endGameScore = it.score()
            }

            when (team.colorMarker) {
                ColorMarker.RED -> colorMarkerRed.isChecked = true
                ColorMarker.BLUE -> colorMarkerBlue.isChecked = true
                ColorMarker.GREEN -> colorMarkerGreen.isChecked = true
                ColorMarker.YELLOW -> colorMarkerYellow.isChecked = true
                else -> colorMarkerDefault.isChecked = true
            }

            when (team.preferredZone) {
                PreferredZone.BUILDING -> rbZoneBuilding.isChecked = true
                PreferredZone.LOADING -> rbZoneLoading.isChecked = true
            }

            etNotes.setText(team.notes)
        }
    }

    private fun parseAutonomousPeriod(content: EditTeamContentDialogBinding) = AutonomousPeriod(
        wobbleDelivery = content.swAutoWobbleDelivery.isChecked,
        lowGoal = content.ctAutoLowGoal.counter,
        midGoal = content.ctAutoMidGoal.counter,
        highGoal = content.ctAutoHighGoal.counter,
        powerShot = content.ctAutoPowerShot.counter,
        parked = content.swAutoParking.isChecked
    )

    private fun parseControlledPeriod(content: EditTeamContentDialogBinding) = ControlledPeriod(
        lowGoal = content.ctLowGoal.counter,
        midGoal = content.ctMidGoal.counter,
        highGoal = content.ctHighGoal.counter
    )

    private fun parseEndGameData(content: EditTeamContentDialogBinding) = EndGamePeriod(
        powerShot = content.ctPowerShot.counter,
        wobbleRings = content.ctWobbleRings.counter,
        wobbleDeliveryZone = when (content.toggleWobbleDelivery.checkedButtonId) {
            content.rbWobbleDeliveryStartLine.id -> WobbleDeliveryZone.START_LINE
            content.rbWobbleDeliveryDeadZone.id -> WobbleDeliveryZone.DEAD_ZONE
            else -> WobbleDeliveryZone.NONE
        }
    )

    private fun parseColorMarker(): Int {
        val content = binding.content

        return when (content.groupColorMarker.checkedChipId) {
            content.colorMarkerRed.id -> ColorMarker.RED
            content.colorMarkerBlue.id -> ColorMarker.BLUE
            content.colorMarkerGreen.id -> ColorMarker.GREEN
            content.colorMarkerYellow.id -> ColorMarker.YELLOW
            else -> ColorMarker.DEFAULT
        }
    }

    private fun updateAutonomousScore(score: Int = -1) {
        val newScore =
            if (score == -1) parseAutonomousPeriod(binding.content).score() else score

        autonomousScore = newScore
        binding.content.tvAutonomousScore.text = getString(R.string.autonomous_score, newScore)
    }

    private fun updateControlledScore(score: Int = -1) {
        val newScore =
            if (score == -1) parseControlledPeriod(binding.content).score() else score

        teleOpScore = newScore
        binding.content.tvDriverControlledScore.text =
            getString(R.string.driver_controlled_score, newScore)
    }

    private fun updateEndGameScore(score: Int = -1) {
        val newScore = if (score == -1) parseEndGameData(binding.content).score() else score

        endGameScore = newScore
        binding.content.tvEndgameScore.text = getString(R.string.endgame_score, newScore)
    }

    private fun updateTotalScore() {
        val totalScore = autonomousScore + teleOpScore + endGameScore

        binding.content.tvTotalScore.text = getString(R.string.total_score, totalScore)
    }
}
