package net.gearmaniacs.tournament.interfaces

interface RecyclerViewItemListener<T> {

    fun onClickListener(item: T)

    fun onLongClickListener(item: T)
}
