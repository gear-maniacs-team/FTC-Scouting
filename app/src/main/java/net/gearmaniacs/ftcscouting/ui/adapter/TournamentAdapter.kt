package net.gearmaniacs.ftcscouting.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.tournament.interfaces.RecyclerViewItemListener

class TournamentAdapter(
    private val listener: RecyclerViewItemListener<Tournament>
) : RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder>() {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Tournament>() {
            override fun areItemsTheSame(oldTeam: Tournament, newTeam: Tournament) =
                oldTeam.key == newTeam.key

            override fun areContentsTheSame(oldTeam: Tournament, newTeam: Tournament) =
                oldTeam == newTeam
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    init {
        setHasStableIds(true)
    }

    fun getItem(position: Int): Tournament = differ.currentList[position]

    fun submitList(list: List<Tournament>) {
        differ.submitList(list)
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemId(position: Int) = getItem(position).key.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentViewHolder {
        val holder = TournamentViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_tournament,
                parent,
                false
            )
        )

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != -1)
                listener.onClickListener(getItem(pos))
        }

        holder.itemView.setOnLongClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != -1)
                listener.onLongClickListener(getItem(pos))

            true
        }

        return holder
    }

    override fun onBindViewHolder(holder: TournamentViewHolder, position: Int) {
        val tournament = getItem(position)
        holder.tvName.text = tournament.name
    }

    class TournamentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_tournament_name)
    }
}
