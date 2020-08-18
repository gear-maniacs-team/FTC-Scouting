package net.gearmaniacs.core.utils

import androidx.recyclerview.widget.RecyclerView

abstract class OneElementRecyclerViewAdapter<VH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<VH>() {

    var isVisible = true
        set(value) {
            if (field == value)
                return
            field = value

            //notifyDataSetChanged()
            if (value)
                notifyItemInserted(0)
            else
                notifyItemRemoved(0)
        }

    override fun getItemCount(): Int = if (isVisible) 1 else 0
}
