package com.andreibelous.plankdetektor.data

import com.andreibelous.plankdetektor.findAngle

data class Person(val keyPoints: List<KeyPoint>, val score: Float)

fun Person.isInPlank(): Boolean {

    // right
    var angle1 =
        findAngle(
            keyPoints[plankPointsRight[0].first.position].coordinate,
            keyPoints[plankPointsRight[0].second.position].coordinate,
            keyPoints[plankPointsRight[0].third.position].coordinate,
        )

    var angle2 =
        findAngle(
            keyPoints[plankPointsRight[1].first.position].coordinate,
            keyPoints[plankPointsRight[1].second.position].coordinate,
            keyPoints[plankPointsRight[1].third.position].coordinate,
        )

    var angle3 =
        findAngle(
            keyPoints[plankPointsRight[2].first.position].coordinate,
            keyPoints[plankPointsRight[2].second.position].coordinate,
            keyPoints[plankPointsRight[2].third.position].coordinate,
        )

    var angle4 =
        findAngle(
            keyPoints[plankPointsRight[3].first.position].coordinate,
            keyPoints[plankPointsRight[3].second.position].coordinate,
            keyPoints[plankPointsRight[3].third.position].coordinate,
        )


    if (angle1 > 150f && angle2 > 150 && angle3 in 60.0..120.0 && angle4 in 60.0..130.0) {
        return true
    }

    // left

    angle1 =
        findAngle(
            keyPoints[plankPointsLeft[0].first.position].coordinate,
            keyPoints[plankPointsLeft[0].second.position].coordinate,
            keyPoints[plankPointsLeft[0].third.position].coordinate,
        )

    angle2 =
        findAngle(
            keyPoints[plankPointsLeft[1].first.position].coordinate,
            keyPoints[plankPointsLeft[1].second.position].coordinate,
            keyPoints[plankPointsLeft[1].third.position].coordinate,
        )

    angle3 =
        findAngle(
            keyPoints[plankPointsLeft[2].first.position].coordinate,
            keyPoints[plankPointsLeft[2].second.position].coordinate,
            keyPoints[plankPointsLeft[2].third.position].coordinate,
        )

    angle4 =
        findAngle(
            keyPoints[plankPointsLeft[3].first.position].coordinate,
            keyPoints[plankPointsLeft[3].second.position].coordinate,
            keyPoints[plankPointsLeft[3].third.position].coordinate,
        )

    if (angle1 > 150f && angle2 > 150 && angle3 in 60.0..120.0 && angle4 in 60.0..120.0) {
        return true
    }

    return false
}

private val plankPointsRight =
    listOf(
        Triple(BodyPart.RIGHT_ANKLE, BodyPart.RIGHT_KNEE, BodyPart.RIGHT_HIP),
        Triple(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
        Triple(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Triple(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST)
    )

private val plankPointsLeft =
    listOf(
        Triple(BodyPart.LEFT_ANKLE, BodyPart.LEFT_KNEE, BodyPart.LEFT_HIP),
        Triple(BodyPart.LEFT_KNEE, BodyPart.LEFT_HIP, BodyPart.LEFT_SHOULDER),
        Triple(BodyPart.LEFT_HIP, BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Triple(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
    )