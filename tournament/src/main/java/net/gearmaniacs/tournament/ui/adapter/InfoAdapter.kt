package net.gearmaniacs.tournament.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.tournament.R

internal class InfoAdapter : RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Match>() {
            override fun areItemsTheSame(oldMatch: Match, newMatch: Match) =
                oldMatch.key == newMatch.key

            override fun areContentsTheSame(oldMatch: Match, newMatch: Match) = oldMatch == newMatch
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    init {
        setHasStableIds(true)
    }

    private fun getItem(position: Int): Match = differ.currentList[position]

    fun submitList(list: List<Match>) {
        differ.submitList(list)
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemId(position: Int) = getItem(position).key.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        return InfoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)
        )
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMatchId: TextView = itemView.findViewById(R.id.tv_match_id)
        private val tvRedAlliance: TextView = itemView.findViewById(R.id.tv_match_red_alliance)
        private val tvBlueAlliance: TextView = itemView.findViewById(R.id.tv_match_blue_alliance)
        private val tvWinner: TextView = itemView.findViewById(R.id.tv_match_detailed_info)

        @SuppressLint("SetTextI18n")
        fun bind(match: Match) {
            val context = itemView.context

            tvMatchId.text = "#${match.id}"
            tvRedAlliance.text = context.getString(
                R.string.match_info_red_alliance,
                match.redAlliance.firstTeam,
                match.redAlliance.secondTeam
            )
            tvBlueAlliance.text = context.getString(
                R.string.match_info_blue_alliance,
                match.blueAlliance.firstTeam,
                match.blueAlliance.secondTeam
            )

            val redScore = match.redAlliance.score
            val blueScore = match.blueAlliance.score

            if (redScore != 0 && blueScore != 0) {
                val winner = context.getString(
                    when {
                        blueScore > redScore -> R.string.match_blue_wins
                        blueScore < redScore -> R.string.match_red_wins
                        else -> R.string.match_draw
                    }
                )

                tvWinner.text = context.getString(
                    R.string.match_basic_info,
                    match.redAlliance.score,
                    match.blueAlliance.score,
                    winner
                )
                tvWinner.isVisible = true
            } else {
                tvWinner.isVisible = false
            }
        }
    }
}
