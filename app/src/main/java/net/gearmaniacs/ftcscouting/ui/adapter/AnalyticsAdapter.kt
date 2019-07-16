package net.gearmaniacs.ftcscouting.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.core.model.TeamPower

class AnalyticsAdapter : RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<TeamPower> = object : DiffUtil.ItemCallback<TeamPower>() {
            override fun areItemsTheSame(old: TeamPower, new: TeamPower) = old.id == new.id

            override fun areContentsTheSame(old: TeamPower, new: TeamPower) = old == new
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)
    private var highestScore = 0

    init {
        setHasStableIds(true)
    }

    fun getItem(position: Int): TeamPower = differ.currentList[position]

    fun submitList(list: List<TeamPower>) {
        highestScore = list[0].power.toInt()
        differ.submitList(list)
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemId(position: Int) = getItem(position).id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AnalyticsViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_analytics,
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        val team = getItem(position)
        val context = holder.itemView.context

        holder.tvName.text = context.getString(R.string.team_id_name, team.id, team.name)
        holder.tvScore.text = team.power.toString()

        holder.pbScore.max = highestScore
        holder.pbScore.progress = team.power.toInt()
    }

    class AnalyticsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = itemView.findViewById(R.id.tv_team_name)
        val tvScore: TextView = itemView.findViewById(R.id.tv_team_score)
        val pbScore: ProgressBar = itemView.findViewById(R.id.pb_team_score)
    }
}