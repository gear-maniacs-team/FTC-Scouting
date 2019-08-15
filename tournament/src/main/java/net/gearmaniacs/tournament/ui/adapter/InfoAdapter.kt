package net.gearmaniacs.tournament.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.tournament.R

internal class InfoAdapter : RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Match> = object : DiffUtil.ItemCallback<Match>() {
            override fun areItemsTheSame(oldMatch: Match, newMatch: Match) = oldMatch.key == newMatch.key

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
