package net.gearmaniacs.ftcscouting.ui.adapter

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
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.model.PreferredLocation
import net.gearmaniacs.ftcscouting.model.Team
import net.gearmaniacs.ftcscouting.utils.DataRecyclerListener

class TeamAdapter(
    private val recyclerView: RecyclerView,
    private val listener: DataRecyclerListener
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Team> = object : DiffUtil.ItemCallback<Team>() {
            override fun areItemsTheSame(oldTeam: Team, newTeam: Team) = oldTeam.key == newTeam.key

            override fun areContentsTheSame(oldTeam: Team, newTeam: Team) = oldTeam == newTeam
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    init {
        setHasStableIds(true)
    }

    fun getItem(position: Int): Team = differ.currentList[position]

    fun submitList(list: List<Team>) {
        differ.submitList(list)
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemId(position: Int) = getItem(position).key.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val holder = TeamViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false))

        holder.itemView.setOnClickListener {
            val expanded = holder.btnEdit.isVisible

            holder.ivExpand.animate()
                .setDuration(300L)
                .rotation(if (expanded) 0f else 180f)
                .start()

            TransitionManager.beginDelayedTransition(recyclerView)

            holder.tvDescription.isVisible = !expanded
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

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = getItem(position)
        holder.bind(team)
    }

    override fun onViewRecycled(holder: TeamViewHolder) {
        holder.ivExpand.rotation = 0f
        holder.tvDescription.isVisible = false
        holder.btnEdit.isVisible = false
        holder.btnDelete.isVisible = false
    }

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_team_name)
        val ivExpand: ImageView = itemView.findViewById(R.id.iv_team_expand)
        private val tvScore: TextView = itemView.findViewById(R.id.tv_team_predicted_score)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_team_description)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)

        fun bind(team: Team) {
            val context = itemView.context

            tvName.text = context.getString(R.string.team_id_name, team.id, team.name)
            tvScore.text = context.getString(R.string.team_predicted_score, team.score)

            val preferredLocation = when (team.preferredLocation) {
                PreferredLocation.DEPOT -> R.string.team_preferred_depot
                PreferredLocation.CRATER -> R.string.team_preferred_crater
                else -> R.string.none
            }
            val description = context.getString(
                R.string.team_description, team.autonomousScore, team.teleOpScore,
                team.endGameScore, context.getString(preferredLocation), team.comments ?: ""
            )
            tvDescription.text = description
        }
    }
}
