package net.gearmaniacs.tournament.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.model.PreferredZone
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.view.ExpandableLayout
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.utils.RecyclerViewItemListener

internal class TeamAdapter(
    private val listener: RecyclerViewItemListener
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    companion object {
        private const val EXPAND_ANIMATION_DURATION = 280L

        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Team> =
            object : DiffUtil.ItemCallback<Team>() {
                override fun areItemsTheSame(oldTeam: Team, newTeam: Team) =
                    oldTeam.key == newTeam.key

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
        val view = ExpandableLayout(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val holder = TeamViewHolder(view)

        holder.btnEdit.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != -1)
                listener.onClickListener(pos)
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != -1)
                listener.onLongClickListener(pos)
        }

        return holder
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = getItem(position)
        holder.bind(team)
    }

    override fun onViewRecycled(holder: TeamViewHolder) {
        holder.card.collapse(false)
    }

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card = itemView as ExpandableLayout
        private val tvName: TextView = itemView.findViewById(R.id.tv_card_title)
        private val tvScore: TextView = itemView.findViewById(R.id.tv_card_primary_desc)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_card_secondary_desc)
        val btnEdit: Button = itemView.findViewById(R.id.btn_card_main_action)
        val btnDelete: Button = itemView.findViewById(R.id.btn_card_secondary_action)

        init {
            card.expandDuration = EXPAND_ANIMATION_DURATION
        }

        fun bind(team: Team) {
            val context = itemView.context

            tvName.text = context.getString(R.string.team_id_name, team.id, team.name.orEmpty())
            tvScore.text = context.getString(R.string.team_predicted_score, team.score)

            val preferredLocation = when (team.preferredZone) {
                PreferredZone.BUILDING -> R.string.team_preferred_building
                PreferredZone.LOADING -> R.string.team_preferred_loading
                else -> R.string.none
            }

            val description = context.getString(
                R.string.team_description, team.autonomousScore, team.teleOpScore,
                team.endGameScore, context.getString(preferredLocation), team.notes.orEmpty()
            )
            tvDescription.text = description
        }
    }
}
