package com.andreibelous.plankdetektor

import android.graphics.PointF
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