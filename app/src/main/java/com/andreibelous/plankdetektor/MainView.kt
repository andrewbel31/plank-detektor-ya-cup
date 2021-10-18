package com.andreibelous.plankdetektor

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.andreibelous.plankdetektor.feature.Stage
import com.andreibelous.plankdetektor.feature.data.Attempt
import com.andreibelous.plankdetektor.view.results.ResultsView
import com.andreibelous.plankdetektor.view.results.ResultsViewModel
import com.andreibelous.plankdetektor.view.results.StrAttempt
import com.andreibelous.plankdetektor.view.results.dp
import com.badoo.mvicore.modelWatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

class MainView(
    private val root: AppCompatActivity,
    private val events: PublishRelay<Event> = PublishRelay.create()
) : Consumer<MainViewModel>, ObservableSource<MainView.Event> by events {

    sealed interface Event {

        object CloseClicked : Event
        object ShowResultsClicked : Event
    }

    private var timer: Disposable? = null
    private var dialog: AlertDialog? = null
    private val buttonResults = root.findViewById<TextView>(R.id.label_results).apply {
        val radii = context.dp(24f)
        val stroke = context.dp(2f)
        val radiiArr = floatArrayOf(radii, radii, radii, radii, radii, radii, radii, radii)
        background =
            RippleDrawable(
                ColorStateList.valueOf(Color.YELLOW),
                GradientDrawable().apply {
                    setStroke(stroke.toInt(), Color.YELLOW)
                    cornerRadii = radiiArr
                },
                ShapeDrawable(RoundRectShape(radiiArr, null, null))
            )

        setOnClickListener { events.accept(Event.ShowResultsClicked) }
        setTextColor(Color.YELLOW)
    }

    private val resultsView = root.findViewById<ResultsView>(R.id.results_view)
    private val labelTimer = root.findViewById<TextView>(R.id.label_timer).apply {
        setTextColor(Color.YELLOW)
    }
    private val dimOverlay =
        root.findViewById<View>(R.id.dim_overlay)
            .apply {
                alpha = 0.0f
                gone()
            }

    private val buttonClose = root.findViewById<View>(R.id.button_close).apply {
        setOnClickListener { events.accept(Event.CloseClicked) }
        background = RippleDrawable(
            ColorStateList.valueOf(Color.YELLOW),
            null,
            ShapeDrawable(OvalShape())
        )
    }

    private val behaviour = BottomSheetBehavior.from(resultsView).apply {
        addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> dimOverlay.gone()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val progress = maxOf(slideOffset, 0f)
                    dimOverlay.alpha = progress
                }
            }
        )
    }

    private val action = root.findViewById<TextView>(R.id.label_action).apply {
        setTextColor(Color.YELLOW)
    }

    init {
        dimOverlay.setOnClickListener { behaviour.state = BottomSheetBehavior.STATE_COLLAPSED }
        root.lifecycle.subscribe {
            dialog?.dismiss()
            dialog = null
            timer?.dispose()
            timer = null
        }
    }

    override fun accept(vm: MainViewModel) {
        modelWatcher(vm)
    }

    private val modelWatcher = modelWatcher<MainViewModel> {
        watch(MainViewModel::stage) { stage ->
            when (stage) {
                is Stage.Init -> init()
                is Stage.InProgress -> inProgress()
            }
        }
    }

    private fun init() {
        if (timer != null) {
            timer?.dispose()
            timer = null
            labelTimer.text = ""
            labelTimer.gone()
        }
        hideResults()
        action.text = "Встаньте в планку"
    }

    private fun inProgress() {
        if (timer == null) {
            val start = System.currentTimeMillis()
            timer = Observable.interval(10L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    val time = System.currentTimeMillis() - start
                    labelTimer.text = time.formatDuration()
                }
            labelTimer.text = ""
            labelTimer.visible()
        }
        hideResults()
        action.text = "Вы в планке"
    }

    private fun showResults(data: List<Attempt>) {
        dimOverlay.visible()
        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        resultsView.bind(
            ResultsViewModel(
                data
                    .reversed()
                    .map {
                        StrAttempt(
                            date = it.date.formatEndPoints(),
                            duration = "${(it.end - it.start).formatDuration()} сек."
                        )
                    })
        )
    }

    private fun hideResults() {
        behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun execute(action: Action) {
        when (action) {
            is Action.ShowResultsDialog -> showResults(action.data)
            is Action.ShowError -> {
                dialog?.dismiss()
                AlertDialog.Builder(root)
                    .setTitle("Упс...")
                    .setMessage("Что то пошло не так. Попробуете еще раз?")
                    .setPositiveButton("попробовать") { _, _ ->
                        dialog?.dismiss()
                        dialog = null
                    }
                    .setNegativeButton("нет") { _, _ ->
                        dialog?.dismiss()
                        dialog = null
                    }
                    .setCancelable(true)
                    .create()
                    .also { dialog = it }
                    .show()
            }
        }
    }

    sealed interface Action {

        data class ShowResultsDialog(val data: List<Attempt>) : Action
        object ShowError : Action
    }
}

data class MainViewModel(
    val stage: Stage
)