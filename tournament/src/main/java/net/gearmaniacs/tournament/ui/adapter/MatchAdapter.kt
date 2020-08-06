package net.gearmaniacs.tournament.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.toSpanned
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.view.ExpandableLayout
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener

internal class MatchAdapter(
    private val listener: RecyclerViewItemListener<Match>
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    companion object {
        private const val EXPAND_ANIMATION_DURATION = 280L

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Match>() {
            override fun areItemsTheSame(oldMatch: Match, newMatch: Match) =
                oldMatch.key == newMatch.key

            override fun areContentsTheSame(oldMatch: Match, newMatch: Match) =
                oldMatch == newMatch
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = ExpandableLayout(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val holder = MatchViewHolder(view)

        holder.btnEdit.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != -1)
                listener.onClickListener(getItem(pos))
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != -1)
                listener.onLongClickListener(getItem(pos))
        }

        return holder
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = getItem(position)
        holder.bind(match)
    }

    override fun onViewRecycled(holder: MatchViewHolder) {
        holder.layout.collapse(false)
    }

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout = itemView as ExpandableLayout
        private val tvMatchId: TextView = itemView.findViewById(R.id.tv_card_title)
        private val tvBasicInfo: TextView = itemView.findViewById(R.id.tv_card_primary_desc)
        private val tvDetailedInfo: TextView = itemView.findViewById(R.id.tv_card_secondary_desc)
        val btnEdit: Button = itemView.findViewById(R.id.btn_card_main_action)
        val btnDelete: Button = itemView.findViewById(R.id.btn_card_secondary_action)

        private val blueColor = ContextCompat.getColor(itemView.context, R.color.blueAlliance)
        private val redColor = ContextCompat.getColor(itemView.context, R.color.redAlliance)

        init {
            layout.expandDuration = EXPAND_ANIMATION_DURATION
        }

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
            val winnerColor = when {
                blueScore > redScore -> blueColor
                blueScore < redScore -> redColor
                else -> Color.WHITE
            }

            val builder = SpannableStringBuilder("#${match.id}: ").apply {
                append(winner, ForegroundColorSpan(winnerColor), 0)
            }

            tvMatchId.text = builder.toSpanned()
            tvBasicInfo.text = context.getString(
                R.string.match_score_details,
                match.redAlliance.score,
                match.blueAlliance.score
            )
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
