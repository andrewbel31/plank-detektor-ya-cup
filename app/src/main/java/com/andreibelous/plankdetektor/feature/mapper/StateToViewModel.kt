package com.andreibelous.plankdetektor.feature.mapper

import com.andreibelous.plankdetektor.MainViewModel
import com.andreibelous.plankdetektor.feature.PlankState

internal object StateToViewModel : (PlankState) -> MainViewModel {

    override fun invoke(state: PlankState): MainViewModel =
        MainViewModel(
            stage = state.stage
        )
}