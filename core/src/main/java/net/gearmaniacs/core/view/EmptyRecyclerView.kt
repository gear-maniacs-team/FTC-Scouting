package net.gearmaniacs.core.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmptyRecyclerView : RecyclerView {

    private var emptyView: View? = null

    private val emptyObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onChanged() {
            val adapter = adapter
            val emptyView = emptyView

            if (adapter != null && emptyView != null) {
                if (adapter.itemCount == 0) {
                    emptyView.visibility = View.VISIBLE
                    this@EmptyRecyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    this@EmptyRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle)

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)

        adapter?.registerAdapterDataObserver(emptyObserver)

        emptyObserver.onChanged()
    }

    fun setEmptyView(view: View) {
        emptyView = view
    }

    fun setFabToHide(fab: FloatingActionButton) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.isOrWillBeShown) {
                    fab.hide()
                } else if (dy < 0 && fab.isOrWillBeHidden) {
                    fab.show()
                }
            }
        })
    }
}