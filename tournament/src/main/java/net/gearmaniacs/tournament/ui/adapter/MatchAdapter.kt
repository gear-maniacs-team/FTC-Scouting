package net.gearmaniacs.tournament.ui.adapter

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.text.toSpanned
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.extensions.getColorCompat
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
        holder.recycle()
    }

    class MatchViewHolder(private val layout: ExpandableLayout) : RecyclerView.ViewHolder(layout) {
        private val tvMatchId: TextView = layout.binding.tvTitle
        private val tvBasicInfo: TextView = layout.binding.tvPrimaryDesc
        private val tvDetailedInfo: TextView = layout.binding.tvSecondaryDesc
        val btnEdit: Button = layout.binding.btnMainAction
        val btnDelete: Button = layout.binding.btnSecondaryAction

        private val blueColor = itemView.context.getColorCompat(R.color.alliance_blue)
        private val redColor = itemView.context.getColorCompat(R.color.alliance_red)

        init {
            layout.expandDuration = EXPAND_ANIMATION_DURATION
        }

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

        fun recycle() {
            layout.collapse(false)
        }
    }
}
