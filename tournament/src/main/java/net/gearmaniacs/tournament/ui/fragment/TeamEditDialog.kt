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
import kotlinx.android.synthetic.main.dialog_edit_team.view.*
import kotlinx.android.synthetic.main.dialog_edit_team_content.*
import kotlinx.android.synthetic.main.dialog_edit_team_content.view.*
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.model.AutonomousData
import net.gearmaniacs.core.model.EndGameData
import net.gearmaniacs.core.model.PreferredZone
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeleOpData
import net.gearmaniacs.tournament.R
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
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.dialog_edit_team, container, false)

        view.bottom_bar.setNavigationIcon(R.drawable.ic_close)
        view.bottom_bar.setNavigationOnClickListener { dismiss() }

        view.bottom_bar.doOnPreDraw { bottom_bar ->
            view.layout_content.updatePadding(bottom = (bottom_bar.height * 1.6f).toInt())
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

        view.cb_cap_placed.setOnCheckedChangeListener { _, isChecked ->
            layout_cap_level.isInvisible = !isChecked
            updateEndGameScore(view)
            updateTotalScore(view)
        }

        view.fab_edit_team_done.setOnClickListener {
            // Parse Team data
            val autonomousData = parseAutonomousData(view)
            val teleOpData = parseTeleOpData(view)
            val endGameData = parseEndGameData(view)

            val preferredZone = when {
                rb_zone_building.isChecked -> PreferredZone.BUILDING
                rb_zone_loading.isChecked -> PreferredZone.LOADING
                else -> PreferredZone.NONE
            }

            val notesText = view.et_notes.getTextString()
            val parsedTeam = Team(
                id = view.et_team_number.getTextString().toIntOrDefault(),
                name = view.et_team_name.getTextString(),
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

        setupListeners(view)

        if (team != null)
            restoreTeamData(view, team)

        updateAutonomousScore(view, autonomousScore)
        updateTeleOpScore(view, teleOpScore)
        updateEndGameScore(view, endGameScore)
        updateTotalScore(view)
    }

    private fun setupListeners(view: View) {
        val autonomousListener = DataChangeListener {
            updateAutonomousScore(view)
            updateTotalScore(view)
        }
        val teleOpListener = DataChangeListener {
            updateTeleOpScore(view)
            updateTotalScore(view)
        }
        val endGameListener = DataChangeListener {
            updateEndGameScore(view)
            updateTotalScore(view)
        }

        view.cb_reposition_foundation.setOnCheckedChangeListener(autonomousListener)
        view.cb_navigated.setOnCheckedChangeListener(autonomousListener)
        view.et_auto_delivered_skystones.addTextChangedListener(autonomousListener)
        view.et_auto_delivered_stones.addTextChangedListener(autonomousListener)
        view.et_auto_placed_stones.addTextChangedListener(autonomousListener)

        view.et_delivered_stones.addTextChangedListener(teleOpListener)
        view.et_placed_stones.addTextChangedListener(teleOpListener)
        view.et_skyscraper_height.addTextChangedListener(teleOpListener)

        view.cb_move_foundation.setOnCheckedChangeListener(endGameListener)
        view.cb_parking.setOnCheckedChangeListener(endGameListener)
        view.et_cap_level.addTextChangedListener(endGameListener)
    }

    private fun restoreTeamData(view: View, team: Team) {
        with(view) {
            et_team_number.setText(team.id.toString())
            et_team_name.setText(team.name)

            team.autonomousData?.let {
                cb_reposition_foundation.isChecked = it.repositionFoundation
                cb_navigated.isChecked = it.navigated
                et_auto_delivered_skystones.setText(it.deliveredSkystones.toString())
                et_auto_delivered_stones.setText(it.deliveredStones.toString())
                et_auto_placed_stones.setText(it.placedStones.toString())

                autonomousScore = it.calculateScore()
            }

            team.teleOpData?.let {
                et_delivered_stones.setText(it.deliveredStones.toString())
                et_placed_stones.setText(it.placedStones.toString())
                et_skyscraper_height.setText(it.skyscraperHeight.toString())

                teleOpScore = it.calculateScore()
            }

            team.endGameData?.let {
                cb_move_foundation.isChecked = it.moveFoundation
                cb_parking.isChecked = it.parked

                if (it.capLevel > 0) {
                    cb_cap_placed.isChecked = true
                    et_cap_level.setText(it.capLevel.toString())
                }

                endGameScore = it.calculateScore()
            }

            when (team.preferredZone) {
                PreferredZone.BUILDING -> rb_zone_building.isChecked = true
                PreferredZone.LOADING -> rb_zone_loading.isChecked = true
            }

            et_notes.setText(team.notes)
        }
    }

    private fun parseAutonomousData(view: View) = AutonomousData(
        view.cb_reposition_foundation.isChecked,
        view.cb_navigated.isChecked,
        view.et_auto_delivered_skystones.getTextString().toIntOrDefault(),
        view.et_auto_delivered_stones.getTextString().toIntOrDefault(),
        view.et_auto_placed_stones.getTextString().toIntOrDefault()
    )

    private fun parseTeleOpData(view: View) = TeleOpData(
        view.et_delivered_stones.getTextString().toIntOrDefault(),
        view.et_placed_stones.getTextString().toIntOrDefault(),
        view.et_skyscraper_height.getTextString().toIntOrDefault()
    )

    private fun parseEndGameData(view: View): EndGameData {
        val capLevel =
            if (view.cb_cap_placed.isChecked) view.et_cap_level.getTextString().toIntOrDefault() else 0

        return EndGameData(
            view.cb_move_foundation.isChecked,
            view.cb_parking.isChecked,
            capLevel
        )
    }

    private fun updateAutonomousScore(view: View, score: Int = -1) {
        val newScore = if (score == -1) parseAutonomousData(view).calculateScore() else score

        autonomousScore = newScore
        view.tv_autonomous_score.text = getString(R.string.autonomous_score, newScore)
    }

    private fun updateTeleOpScore(view: View, score: Int = -1) {
        val newScore = if (score == -1) parseTeleOpData(view).calculateScore() else score

        teleOpScore = newScore
        view.tv_teleop_score.text = getString(R.string.teleop_score, newScore)
    }

    private fun updateEndGameScore(view: View, score: Int = -1) {
        val newScore = if (score == -1) parseEndGameData(view).calculateScore() else score

        endGameScore = newScore
        view.tv_endgame_score.text = getString(R.string.endgame_score, newScore)
    }

    private fun updateTotalScore(view: View) {
        val totalScore = autonomousScore + teleOpScore + endGameScore

        view.tv_total_score.text = getString(R.string.total_score, totalScore)
    }
}
