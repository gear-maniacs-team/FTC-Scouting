package net.gearmaniacs.tournament.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import net.gearmaniacs.tournament.R

class SearchAdapter(
    private val queryListener: QueryListener
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    var showSearchBar = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val holder = SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_search_bar, parent, false)
        )

        holder.searchBar.addTextChangedListener(afterTextChanged = queryListener::onQueryChange)

        return holder
    }

    override fun getItemCount(): Int = if (showSearchBar) 1 else 0

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) = Unit

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchBar: TextInputEditText = itemView.findViewById(R.id.et_search_bar)
    }

    interface QueryListener {
        fun onQueryChange(newQuery: CharSequence?)
    }
}
