package net.gearmaniacs.ftcscouting.utils

interface DataRecyclerListener {

    fun onEditItem(position: Int)

    fun onDeleteItem(position: Int)
}