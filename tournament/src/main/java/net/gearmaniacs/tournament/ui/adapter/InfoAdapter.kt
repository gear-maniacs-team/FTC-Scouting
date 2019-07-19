package net.gearmaniacs.tournament.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.tournament.R

internal class InfoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TEAM_VIEW_TYPE = 0
        private const val MATCH_VIEW_TYPE = 1
    }

    var teamList = emptyList<Team>()
    var matchList = emptyList<Match>()

    override fun getItemCount() = teamList.size + matchList.size

    override fun getItemViewType(position: Int): Int {
        return when {
            position < teamList.size -> TEAM_VIEW_TYPE
            position - matchList.size < teamList.size -> MATCH_VIEW_TYPE
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TEAM_VIEW_TYPE) {
            TeamAdapter.TeamViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_match,
                    parent,
                    false
                )
            ).apply {
                ivExpand.isVisible = false
            }
        } else {
            BasicMatchViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_match_basic,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TeamAdapter.TeamViewHolder) {

        } else if (holder is BasicMatchViewHolder) {
            holder.bind(matchList[position - teamList.size])
        }
    }

    class BasicMatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMatchId: TextView = itemView.findViewById(R.id.tv_match_id)
        private val tvBasicInfo: TextView = itemView.findViewById(R.id.tv_match_basic_info)
        private val tvDetailedInfo: TextView = itemView.findViewById(R.id.tv_match_detailed_info)

        @SuppressLint("SetTextI18n")
        fun bind(match: Match) {
            val context = itemView.context

            val redScore = match.redAlliance.score
            val blueScore = match.blueAlliance.score
            val winner = context.getString(
                when {
                    blueScore > redScore -> R.string.match_blue_wins
                    blueScore < redScore -> R.string.match_red_wins
                    else -> R.string.match_draw
                }
            )

            tvMatchId.text = "#${match.id}"
            tvBasicInfo.text =
                context.getString(R.string.match_basic_info, match.redAlliance.score, match.blueAlliance.score, winner)
            tvDetailedInfo.text = context.getString(
                R.string.match_detailed_info,
                match.redAlliance.firstTeam,
                match.redAlliance.secondTeam,
                match.blueAlliance.firstTeam,
                match.blueAlliance.secondTeam
            )
        }
    }
}