package net.gearmaniacs.tournament.ui.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import net.gearmaniacs.core.model.ColorMarker
import net.gearmaniacs.core.model.PreferredZone
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.view.ExpandableLayout
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener


internal class TeamAdapter(
    private val listener: RecyclerViewItemListener<Team>
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

    private fun getItem(position: Int): Team = differ.currentList[position]

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

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = getItem(position)
        holder.bind(team)
    }

    override fun onViewRecycled(holder: TeamViewHolder) {
        holder.recycle()
    }

    class TeamViewHolder(private val layout: ExpandableLayout) : RecyclerView.ViewHolder(layout) {
        private val tvName: TextView = layout.binding.tvTitle
        private val tvScore: MaterialTextView = layout.binding.tvPrimaryDesc
        private val tvDescription: TextView = layout.binding.tvSecondaryDesc
        val btnEdit: Button = layout.binding.btnMainAction
        val btnDelete: Button = layout.binding.btnSecondaryAction

        private val outline = ContextCompat.getDrawable(itemView.context, R.drawable.ic_circle_outline)!!

        init {
            layout.expandDuration = EXPAND_ANIMATION_DURATION
        }

        fun bind(team: Team) {
            val context = itemView.context

            tvName.text = context.getString(R.string.team_id_name, team.id, team.name.orEmpty())
            tvScore.text = context.getString(R.string.team_predicted_score, team.score)

            // Color Marker
            if (team.colorMarker != ColorMarker.DEFAULT)
                setupColorMarker(team)

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

        private fun setupColorMarker(team: Team) {
            val context = itemView.context

            val circle = ContextCompat.getDrawable(context, R.drawable.ic_circle)!!
            val color = ColorMarker.getHexColor(team.colorMarker, context)

            circle.mutate().colorFilter = PorterDuffColorFilter(
                ColorMarker.getHexColor(color, context),
                PorterDuff.Mode.SRC_IN
            )

            val finalDrawable = LayerDrawable(arrayOf(circle, outline)).apply {
                setLayerInset(0, 0, 0, 0, 0)
                setLayerInset(1, 0, 0, 0, 0)
            }

            tvScore.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, finalDrawable, null)
        }

        fun recycle() {
            layout.collapse(false)
            tvScore.setCompoundDrawablesRelative(null, null, null, null)
        }
    }
}
