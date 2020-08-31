package net.gearmaniacs.core.view

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import net.gearmaniacs.core.databinding.ExpandableCardViewBinding

class ExpandableLayout : FrameLayout {

    val binding =
        ExpandableCardViewBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        setHeightToZero(false)

        setOnClickListener {
            if (isExpanded) {
                collapse()
            } else {
                expand()
            }
        }
    }

    private fun rotateArrow(rotation: Float, animate: Boolean) {
        ViewCompat.animate(binding.ivExpand)
            .rotation(rotation)
            .withLayer()
            .setDuration(if (animate) expandDuration else 0)
            .start()
    }

    private fun setHeightToZero(animate: Boolean) {
        if (animate) {
            animate(binding.layoutHidden.height, 0)
        } else {
            setContentHeight(0)
        }
    }

    private fun setHeightToContentHeight(animate: Boolean) {
        measureContentView()
        val targetHeight = binding.layoutHidden.measuredHeight
        if (animate) {
            animate(0, targetHeight)
        } else {
            setContentHeight(targetHeight)
        }
    }

    private fun setContentHeight(height: Int) {
        with(binding.layoutHidden) {
            layoutParams.height = height
            requestLayout()
        }
    }

    private fun measureContentView() {
        val widthMS = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val heightMS = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        binding.layoutHidden.measure(widthMS, heightMS)
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

            with(binding.layoutHidden) {
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
    fun expand(animate: Boolean = true) {
        if (isExpanded) return

        setHeightToContentHeight(animate)
        rotateArrow(180f, animate)
        isExpanded = true
    }

    /**
     * Collapse the Card
     */
    fun collapse(animate: Boolean = true) {
        if (!isExpanded) return

        setHeightToZero(animate)
        rotateArrow(0f, animate)
        isExpanded = false
    }

    /**
     * @property title The title of the card
     */
    var title: CharSequence
        get() = binding.tvTitle.text
        set(title) {
            binding.tvTitle.text = title
        }

    /**
     * Sets the title of the card
     * @param resId String resource to display as title
     * @see title
     */
    fun setTitle(@StringRes resId: Int) {
        title = context.getString(resId)
    }

    /**
     * @property expandDuration The duration of the expand animation
     * @throws IllegalArgumentException if the duration is <= 0
     */
    var expandDuration: Long = 400
        set(duration) {
            if (duration > 0) {
                field = duration
            } else {
                throw IllegalArgumentException("Card Expand Duration can not be smaller than or equal to 0")
            }
        }
}
