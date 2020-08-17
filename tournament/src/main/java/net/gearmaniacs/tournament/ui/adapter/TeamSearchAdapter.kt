package net.gearmaniacs.tournament.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import net.gearmaniacs.core.utils.OneElementRecyclerViewAdapter
import net.gearmaniacs.tournament.R

class TeamSearchAdapter(
    private val queryListener: QueryListener
) : OneElementRecyclerViewAdapter<TeamSearchAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val holder = SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.team_search_view, parent, false)
        )

        holder.searchBar.addTextChangedListener(afterTextChanged = queryListener::onQueryChange)

        return holder
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) = Unit

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchBar: TextInputEditText = itemView.findViewById(R.id.et_search_bar)
    }

    fun interface QueryListener {
        fun onQueryChange(newQuery: CharSequence?)
    }
}
