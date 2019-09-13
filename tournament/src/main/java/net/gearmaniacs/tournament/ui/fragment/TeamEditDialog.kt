package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        fun newInstance(team: Team) = TeamEditDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARG_TEAM, team)
            arguments = bundle
        }
    }

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
        }

        view.fab_edit_team_done.setOnClickListener {
            // Parse Team data
            val autonomousData = AutonomousData(
                view.cb_reposition_foundation.isChecked,
                view.cb_navigated.isChecked,
                view.et_auto_delivered_skystones.getTextString().toIntOrDefault(),
                view.et_auto_delivered_stones.getTextString().toIntOrDefault(),
                view.et_auto_placed_stones.getTextString().toIntOrDefault()
            )

            val teleOpData = TeleOpData(
                view.et_delivered_stones.getTextString().toIntOrDefault(),
                view.et_placed_stones.getTextString().toIntOrDefault()
            )

            val capLevel =
                if (view.cb_cap_placed.isChecked) view.et_cap_level.getTextString().toIntOrDefault() else 0
            val endGameData = EndGameData(
                view.cb_move_foundation.isChecked,
                view.cb_parking.isChecked,
                capLevel
            )

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

        team ?: return

        view.apply {
            et_team_number.setText(team.id.toString())
            et_team_name.setText(team.name)

            team.autonomousData?.let {
                cb_reposition_foundation.isChecked = it.repositionFoundation
                cb_navigated.isChecked = it.navigated
                et_auto_delivered_skystones.setText(it.deliveredSkystones.toString())
                et_auto_delivered_stones.setText(it.deliveredStones.toString())
                et_auto_placed_stones.setText(it.placedStones.toString())
            }

            team.teleOpData?.let {
                et_delivered_stones.setText(it.deliveredStones.toString())
                et_placed_stones.setText(it.placedStones.toString())
            }

            team.endGameData?.let {
                cb_move_foundation.isChecked = it.moveFoundation
                cb_parking.isChecked = it.parked

                if (it.capLevel > 0) {
                    cb_cap_placed.isChecked = true
                    et_cap_level.setText(it.capLevel.toString())
                }
            }

            when (team.preferredZone) {
                PreferredZone.BUILDING -> rb_zone_building.isChecked = true
                PreferredZone.LOADING -> rb_zone_loading.isChecked = true
            }

            et_notes.setText(team.notes)
        }
    }
}
