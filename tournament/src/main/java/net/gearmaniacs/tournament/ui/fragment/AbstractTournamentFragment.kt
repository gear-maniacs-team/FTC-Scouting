package net.gearmaniacs.tournament.ui.fragment

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

internal abstract class AbstractTournamentFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    abstract fun fabClickListener()

    abstract fun getFragmentTag(): String

    interface ICompanion {
        val fragmentTag: String

        fun newInstance(): AbstractTournamentFragment
    }
}
