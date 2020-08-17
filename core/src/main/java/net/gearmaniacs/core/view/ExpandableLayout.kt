package net.gearmaniacs.core.view

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import net.gearmaniacs.core.R

open class ExpandableLayout : FrameLayout {

    private val tvTitle by bind<TextView>(R.id.tv_card_title)
    private val ivArrow by bind<ImageView>(R.id.iv_card_expand)
    private val hiddenLayout by bind<View>(R.id.layout_hidden)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.expandable_card_view, this)

        setHeightToZero(false)

        setOnClickListener {
            if (isExpanded) {
                collapse()
            } else {
                expand()
            }
        }
    }

    private fun <T : View> View.bind(@IdRes res: Int) =
        lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }

    private fun rotateArrow(rotation: Float, animate: Boolean) {
        ViewCompat.animate(ivArrow)
            .rotation(rotation)
            .withLayer()
            .setDuration(if (animate) expandDuration else 0)
            .start()
    }

    private fun setHeightToZero(animate: Boolean) {
        if (animate) {
            animate(hiddenLayout.height, 0)
        } else {
            setContentHeight(0)
        }
    }

    private fun setHeightToContentHeight(animate: Boolean) {
        measureContentView()
        val targetHeight = hiddenLayout.measuredHeight
        if (animate) {
            animate(0, targetHeight)
        } else {
            setContentHeight(targetHeight)
        }
    }

    private fun setContentHeight(height: Int) {
        hiddenLayout.layoutParams.height = height
        hiddenLayout.requestLayout()
    }

    private fun measureContentView() {
        val widthMS = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val heightMS = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        hiddenLayout.measure(widthMS, heightMS)
    }

    private fun animate(from: Int, to: Int) {
        val alphaValues = if (from > to) {
            floatArrayOf(1f, 0f)
        } else {
            floatArrayOf(0f, 1f)
        }

        val heightValuesHolder = PropertyValuesHolder.ofInt("height", from, to)
        val alphaValuesHolder = PropertyValuesHolder.ofFloat("alpha", *alphaValues)

        val animator = ValueAnimator.ofPropertyValuesHolder(heightValuesHolder, alphaValuesHolder)
        animator.duration = expandDuration
        animator.addUpdateListener {
            val newHeight = animator.getAnimatedValue("height") as Int? ?: 0
            val newAlpha = animator.getAnimatedValue("alpha") as Float? ?: 0f

            with(hiddenLayout) {
                layoutParams.height = newHeight
                alpha = newAlpha
                requestLayout()
            }

            invalidate()
        }
        animator.start()
    }

    /**
     * Check if the card is expanded
     */
    var isExpanded = false
        private set

    /**
     * Expand the Card
     */
    open fun expand(animate: Boolean = true) {
        if (isExpanded) return

        setHeightToContentHeight(animate)
        rotateArrow(180f, animate)
        isExpanded = true
    }

    /**
     * Collapse the Card
     */
    open fun collapse(animate: Boolean = true) {
        if (!isExpanded) return

        setHeightToZero(animate)
        rotateArrow(0f, animate)
        isExpanded = false
    }

    /**
     * @property cardTitle The title of the card
     */
    open var cardTitle: CharSequence
        get() = tvTitle.text
        set(title) {
            tvTitle.text = title
        }

    /**
     * Sets the title of the card
     * @param resId String resource to display as title
     * @see cardTitle
     */
    open fun setCardTitle(@StringRes resId: Int) {
        cardTitle = context.getString(resId)
    }

    /**
     * @property expandDuration The duration of the expand animation
     * @throws IllegalArgumentException if the duration is <= 0
     */
    open var expandDuration: Long = 400
        set(duration) {
            if (duration > 0) {
                field = duration
            } else {
                throw IllegalArgumentException("Card Expand Duration can not be smaller than or equal to 0")
            }
        }
}