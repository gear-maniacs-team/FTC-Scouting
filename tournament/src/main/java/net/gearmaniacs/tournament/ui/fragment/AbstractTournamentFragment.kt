package net.gearmaniacs.tournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFade

internal abstract class AbstractTournamentFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    private var inflatedView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFade()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (inflatedView == null) {
            inflatedView = super.onCreateView(inflater, container, savedInstanceState)

            inflatedView?.let {
                onInflateView(it)
            }
        }
        return inflatedView
    }

    /*
     * This method is only called the first time
     * Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle) is called,
     * if the view was inflated
     */
    open fun onInflateView(view: View) = Unit

    abstract fun fabClickListener()

    abstract fun getFragmentTag(): String

    interface ICompanion {
        val fragmentTag: String

        fun newInstance(): AbstractTournamentFragment
    }
}
