package com.andreibelous.plankdetektor.view.results

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.andreibelous.plankdetektor.R

class PlankInfoView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.results_view_phase_item, this)
        layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
    }

    private val title = findViewById<TextView>(R.id.phase_item_title)
    private val duration = findViewById<TextView>(R.id.phase_item_duration)

    fun bind(attempt: StrAttempt) {
        title.text = attempt.date
        duration.text = attempt.duration
    }
}