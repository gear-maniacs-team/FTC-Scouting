package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialFadeThrough

internal abstract class AbstractTournamentFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    abstract fun fabClickListener()

    abstract fun getFragmentTag(): String

    interface ICompanion {
        val fragmentTag: String

        fun newInstance(): AbstractTournamentFragment
    }
}
