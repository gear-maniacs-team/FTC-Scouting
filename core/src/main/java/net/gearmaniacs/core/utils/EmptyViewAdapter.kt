package net.gearmaniacs.core.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import net.gearmaniacs.core.R

class EmptyViewAdapter : OneElementRecyclerViewAdapter<EmptyViewAdapter.ViewHolder>() {

    var text: CharSequence? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.no_content_view, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.isVisible = true
        holder.textView.text = text
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = itemView.findViewById(R.id.empty_view)
    }
}
