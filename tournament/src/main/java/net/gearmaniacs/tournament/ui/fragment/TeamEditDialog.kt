package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.dialog_edit_team.view.*
import kotlinx.android.synthetic.main.dialog_edit_team_content.view.*
import net.gearmaniacs.core.extensions.getTextOrEmpty
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.model.AutonomousData
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

        view.fab_edit_team_done.setOnClickListener {
            // Parse Team data
            val autonomousData = AutonomousData(
                latching = view.cb_latching.isChecked,
                sampling = view.cb_sampling.isChecked,
                marker = view.cb_marker.isChecked,
                parking = view.cb_parking.isChecked,
                minerals = view.et_minerals_collected.getTextOrEmpty().toIntOrDefault(0)
            )

            val teleOpData = TeleOpData(
                depotMinerals = view.et_minerals_depot.getTextOrEmpty().toIntOrDefault(0),
                landerMinerals = view.et_minerals_lander.getTextOrEmpty().toIntOrDefault(0)
            )

            var endGame = 0
            view.rg_end_game.forEachIndexed { index, child ->
                if (child is RadioButton && child.isChecked) {
                    endGame = index
                    return@forEachIndexed
                }
            }

            var preferredLocation = 0
            view.toggle_preferred_location.forEachIndexed { index, child: View? ->
                if (child is MaterialButton && child.isChecked) {
                    preferredLocation = index
                    return@forEachIndexed
                }
            }

            val notesText = view.et_notes.getTextOrEmpty()
            val parsedTeam = Team(
                id = view.et_team_number.getTextOrEmpty().toIntOrDefault(0),
                name = view.et_team_name.getTextOrEmpty(),
                autonomousData = if (autonomousData.isNotEmpty) autonomousData else null,
                teleOpData = if (teleOpData.isNotEmpty) teleOpData else null,
                endGame = endGame,
                preferredLocation = preferredLocation,
                comments = if (notesText.isNotBlank()) notesText else null
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
                cb_latching.isChecked = it.latching
                cb_sampling.isChecked = it.sampling
                cb_marker.isChecked = it.marker
                cb_parking.isChecked = it.parking
                et_minerals_collected.setText(it.minerals.toString())
            }

            team.teleOpData?.let {
                et_minerals_depot.setText(it.depotMinerals.toString())
                et_minerals_lander.setText(it.landerMinerals.toString())
            }

            if (team.endGame != 0) {
                val radioEndGame = rg_end_game[team.endGame] as RadioButton?
                radioEndGame?.isChecked = true
            }

            if (team.preferredLocation != 0) {
                val radioLocation =
                    toggle_preferred_location[team.preferredLocation] as MaterialButton?
                radioLocation?.isChecked = true
            }

            et_notes.setText(team.comments)
        }
    }
}
