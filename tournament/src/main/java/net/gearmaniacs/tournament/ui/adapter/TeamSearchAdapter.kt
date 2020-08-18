package net.gearmaniacs.tournament.ui.adapter

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.android.parcel.Parcelize
import net.gearmaniacs.core.utils.OneElementRecyclerViewAdapter
import net.gearmaniacs.tournament.databinding.TeamSearchViewBinding

class TeamSearchAdapter(
    private val queryListener: QueryListener
) : OneElementRecyclerViewAdapter<TeamSearchAdapter.SearchViewHolder>() {

    private var query = Query(
        name = "",
        defaultMarker = true,
        redMarker = true,
        blueMarker = true,
        greenMarker = true,
        purpleMarker = true,
        yellowMarker = true
    )
    private var isBindingView = false

    fun getQuery() = query

    fun setQuery(newQuery: Query) {
        query = newQuery
        if (isVisible)
            notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val holder = SearchViewHolder(
            TeamSearchViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        val binding = holder.binding

        binding.etSearchBar.addTextChangedListener(afterTextChanged = {
            if (isBindingView)
                return@addTextChangedListener

            query = query.copy(name = it?.toString().orEmpty())
            queryListener.onQueryChange(query)
        })

        binding.groupColorMarker.forEach { child ->
            (child as? Chip)?.setOnCheckedChangeListener { _, _ ->
                if (isBindingView)
                    return@setOnCheckedChangeListener

                query = query.copy(
                    defaultMarker = binding.colorMarkerDefault.isChecked,
                    redMarker = binding.colorMarkerRed.isChecked,
                    blueMarker = binding.colorMarkerBlue.isChecked,
                    greenMarker = binding.colorMarkerGreen.isChecked,
                    purpleMarker = binding.colorMarkerPurple.isChecked,
                    yellowMarker = binding.colorMarkerYellow.isChecked,
                )
                queryListener.onQueryChange(query)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        isBindingView = true

        with(holder.binding) {
            etSearchBar.setText(query.name)

            colorMarkerDefault.isChecked = query.defaultMarker
            colorMarkerRed.isChecked = query.redMarker
            colorMarkerBlue.isChecked = query.blueMarker
            colorMarkerGreen.isChecked = query.greenMarker
            colorMarkerPurple.isChecked = query.purpleMarker
            colorMarkerYellow.isChecked = query.yellowMarker
        }

        isBindingView = false
    }

    class SearchViewHolder(val binding: TeamSearchViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Parcelize
    data class Query(
        val name: String,
        val defaultMarker: Boolean,
        val redMarker: Boolean,
        val blueMarker: Boolean,
        val greenMarker: Boolean,
        val purpleMarker: Boolean,
        val yellowMarker: Boolean
    ) : Parcelable {

        fun isEmpty() =
            name.isEmpty() && defaultMarker && redMarker && blueMarker && greenMarker && purpleMarker && yellowMarker
    }

    fun interface QueryListener {
        fun onQueryChange(newQuery: Query?)
    }
}
