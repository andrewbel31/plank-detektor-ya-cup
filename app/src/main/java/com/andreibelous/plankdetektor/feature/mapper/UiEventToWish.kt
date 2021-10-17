package com.andreibelous.plankdetektor.feature.mapper

import com.andreibelous.plankdetektor.MainView
import com.andreibelous.plankdetektor.feature.PlankFeature.Wish


internal object UiEventToWish : (MainView.Event) -> Wish? {

    override fun invoke(event: MainView.Event): Wish? =
        when (event) {
            is MainView.Event.ShowResultsClicked -> Wish.ShowResults
            else -> null
        }
}