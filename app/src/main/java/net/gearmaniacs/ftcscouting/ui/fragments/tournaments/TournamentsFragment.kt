package net.gearmaniacs.ftcscouting.ui.fragments.tournaments

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class TournamentsFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    abstract fun fabClickListener()

    abstract fun getFragmentTag(): String
}
