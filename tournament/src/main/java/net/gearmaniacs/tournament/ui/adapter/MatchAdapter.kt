package net.gearmaniacs.tournament.ui.adapter

import android.annotation.SuppressLint
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.utils.DataRecyclerViewListener

internal class MatchAdapter(
    private val recyclerView: RecyclerView,
    private val listener: DataRecyclerViewListener
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

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

    fun getItem(position: Int): Match = differ.currentList[position]

    fun submitList(list: List<Match>) {
        differ.submitList(list)
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemId(position: Int) = getItem(position).key.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val holder = MatchViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_match,
                parent,
                false
            )
        )

        holder.itemView.setOnClickListener {
            val expanded = holder.btnEdit.isVisible

            holder.ivExpand.animate()
                .setDuration(300L)
                .rotation(if (expanded) 0f else 180f)
                .start()

            TransitionManager.beginDelayedTransition(recyclerView)

            holder.tvDetailedInfo.isVisible = !expanded
            holder.btnEdit.isVisible = !expanded
            holder.btnDelete.isVisible = !expanded
        }

        holder.btnEdit.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != -1)
                listener.onEditItem(pos)
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != -1)
                listener.onDeleteItem(pos)
        }

        return holder
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = getItem(position)
        holder.bind(match)
    }

    override fun onViewRecycled(holder: MatchViewHolder) {
        holder.ivExpand.rotation = 0f
        holder.tvDetailedInfo.isVisible = false
        holder.btnEdit.isVisible = false
        holder.btnDelete.isVisible = false
    }

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMatchId: TextView = itemView.findViewById(R.id.tv_match_id)
        val ivExpand: ImageView = itemView.findViewById(R.id.iv_match_expand)
        private val tvBasicInfo: TextView = itemView.findViewById(R.id.tv_match_basic_info)
        val tvDetailedInfo: TextView = itemView.findViewById(R.id.tv_match_detailed_info)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)

        @SuppressLint("SetTextI18n")
        fun bind(match: Match) {
            val context = itemView.context

            val redScore = match.redAlliance.score
            val blueScore = match.blueAlliance.score
            val winner = context.getString(
                when {
                    blueScore > redScore -> R.string.match_blue_wins
                    blueScore < redScore -> R.string.match_red_wins
                    blueScore == 0 && redScore == 0 -> R.string.match_not_played
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
