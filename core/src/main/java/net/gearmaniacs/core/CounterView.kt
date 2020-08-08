package net.gearmaniacs.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.getColorOrThrow

class CounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface CounterChange {
        fun onIncrement(count: Int)

        fun onDecrement(count: Int)
    }

    private val tvCounter: TextView
    private val btnIncrement: Button
    private val btnDecrement: Button

    private var counterText = ""
    private var counterMin = 0
    private var counterMax = 100
    private var counterValue = 0

    var counter: Int
        get() = counterValue
        set(value) {
            counterValue = value
            updateView()
        }
    var changeListener: CounterChange? = null

    init {
        inflate(context, R.layout.view_counter, this as ViewGroup)

        tvCounter = findViewById(R.id.tv_counter_text)
        btnIncrement = findViewById(R.id.btn_counter_inc)
        btnDecrement = findViewById(R.id.btn_counter_dec)

        context.theme.obtainStyledAttributes(attrs, R.styleable.CounterView, 0, 0).apply {
            try {
                counterText = getString(R.styleable.CounterView_text).orEmpty()

                try {
                    tvCounter.setTextColor(getColorOrThrow(R.styleable.CounterView_textColor))
                } catch (e: IllegalArgumentException) {
                }

                val textSize = getDimensionPixelSize(R.styleable.CounterView_textSize, -1)
                if (textSize > 0) {
                    tvCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                }

                counterValue = getInt(R.styleable.CounterView_value, 0)
                counterMin = getInt(R.styleable.CounterView_min, 0)
                counterMax = getInt(R.styleable.CounterView_max, 100)
            } finally {
                recycle()
            }
        }

        btnIncrement.setOnClickListener {
            if (counterValue < counterMax) {
                ++counterValue
                changeListener?.onIncrement(counterValue)
            }
            updateView()
        }

        btnDecrement.setOnClickListener {
            if (counterValue > counterMin) {
                --counterValue
                changeListener?.onDecrement(counterValue)
            }
            updateView()
        }

        updateView()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.counterValue = counterValue
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            counterValue = state.counterValue
            super.onRestoreInstanceState(state.superState)
            updateView()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateView() {
        tvCounter.text = "$counterText: $counterValue"
        btnIncrement.isEnabled = counterValue < counterMax
        btnDecrement.isEnabled = counterValue > counterMin
    }

    private class SavedState : BaseSavedState, Parcelable {

        var counterValue = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            counterValue = source.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(counterValue)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState? {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
