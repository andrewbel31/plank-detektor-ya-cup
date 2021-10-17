package com.andreibelous.plankdetektor

import android.graphics.PointF
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

fun findAngle(
    p0: PointF,
    center: PointF,
    p1: PointF,
): Double {
    val p0c =
        sqrt(
            (center.x.toDouble() - p0.x.toDouble()).pow(2.0) +
                    (center.y.toDouble() - p0.y.toDouble()).pow(2.0)
        )
    val p1c =
        sqrt(
            (center.x.toDouble() - p1.x.toDouble()).pow(2.0) +
                    (center.y.toDouble() - p1.y.toDouble()).pow(2.0)
        )
    val p0p1 =
        sqrt(
            (p1.x.toDouble() - p0.x.toDouble()).pow(2.0) +
                    (p1.y.toDouble() - p0.y.toDouble()).pow(2.0)
        )
    val rad = acos((p1c * p1c + p0c * p0c - p0p1 * p0p1) / (2 * p1c * p0c))
    return Math.toDegrees(rad)
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

inline fun <reified T> Any.cast() = this as T

inline fun <reified T> Any.safeCast() = this as? T

fun Long.millisToTime(): String? {
    if (this == 0L) {
        return null
    }
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60

    val secondsStr = seconds.toString()
    val secs: String = if (secondsStr.length >= 2) {
        secondsStr.substring(0, 2)
    } else {
        "0$secondsStr"
    }
    return "$minutes:$secs"
}

inline fun <reified T> T.toObservable() =
    if (this == null) Observable.empty<T>() else Observable.just(this)

inline fun <reified T> ((T) -> Unit).asConsumer(): Consumer<T> =
    Consumer { t -> this@asConsumer.invoke(t) }

fun Lifecycle.subscribe(
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null
) {
    addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            onCreate?.invoke()
        }

        override fun onStart(owner: LifecycleOwner) {
            onStart?.invoke()
        }

        override fun onResume(owner: LifecycleOwner) {
            onResume?.invoke()
        }

        override fun onPause(owner: LifecycleOwner) {
            onPause?.invoke()
        }

        override fun onStop(owner: LifecycleOwner) {
            onStop?.invoke()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            onDestroy?.invoke()
        }
    })
}

private val sdf = SimpleDateFormat("HH:mm:ss.SS", Locale.getDefault())
private val sdfDuration = SimpleDateFormat("ss.SS", Locale.getDefault())
private val calendar = Calendar.getInstance()

fun Long.formatEndPoints(): String {
    calendar.timeInMillis = this
    return sdf.format(calendar.time)
}

fun Long.formatDuration(): String {
    calendar.timeInMillis = this
    return sdfDuration.format(calendar.time)
}