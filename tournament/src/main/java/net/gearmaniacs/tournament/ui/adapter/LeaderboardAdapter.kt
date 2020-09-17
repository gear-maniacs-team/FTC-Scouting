package net.gearmaniacs.tournament.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.extensions.getColorCompat
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.tournament.R

internal class LeaderboardAdapter :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    private companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RankedTeam>() {
            override fun areItemsTheSame(old: RankedTeam, new: RankedTeam) =
                old.number == new.number

            override fun areContentsTheSame(old: RankedTeam, new: RankedTeam) = old == new
        }

        private val PLACE_BACKGROUND_COLORS = intArrayOf(
            R.color.leaderboard_red,
            R.color.leaderboard_blue,
            R.color.leaderboard_green,
            R.color.leaderboard_yellow,
            android.R.color.white,
        )
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    init {
        setHasStableIds(true)
    }

    private fun getItem(position: Int): RankedTeam = differ.currentList[position]

    fun submitList(list: List<RankedTeam>) {
        differ.submitList(list)
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemId(position: Int) = getItem(position).number.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LeaderboardViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.leaderboard_item,
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val team = getItem(position)
        val context = holder.itemView.context

        ImageViewCompat.setImageTintList(
            holder.ivPlaceBackground,
            ColorStateList.valueOf(context.getColorCompat(PLACE_BACKGROUND_COLORS[position % PLACE_BACKGROUND_COLORS.size]))
        )
        holder.tvPlaceNumber.text = (position + 1).toString()
        holder.tvName.text = "${team.number} - ${team.name}"
        holder.tvScore.text = "${team.score} Points"
    }

    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPlaceBackground: ImageView = itemView.findViewById(R.id.iv_place_background)
        val tvPlaceNumber: TextView = itemView.findViewById(R.id.tv_place_number)
        val tvName: TextView = itemView.findViewById(R.id.tv_team_name)
        val tvScore: TextView = itemView.findViewById(R.id.tv_team_score)
    }
}
