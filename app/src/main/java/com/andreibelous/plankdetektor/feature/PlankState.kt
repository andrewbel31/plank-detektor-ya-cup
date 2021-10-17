package com.andreibelous.plankdetektor.feature

data class PlankState(
    val stage: Stage = Stage.Init
)

sealed interface Stage {

    object Init : Stage
    data class InProgress(val startTs: Long) : Stage
}