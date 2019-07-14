package net.gearmaniacs.ftcscouting.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.model.Tournament
import net.gearmaniacs.ftcscouting.utils.DataRecyclerListener

class TournamentAdapter(
    private val listener: DataRecyclerListener
) : RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder>() {

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Tournament> = object : DiffUtil.ItemCallback<Tournament>() {
            override fun areItemsTheSame(oldTeam: Tournament, newTeam: Tournament) = oldTeam.key == newTeam.key

            override fun areContentsTheSame(oldTeam: Tournament, newTeam: Tournament) = oldTeam == newTeam
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
        val holder =
            TournamentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_tournament, parent, false))

        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != -1)
                listener.onEditItem(pos)
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
