package com.andreibelous.plankdetektor.feature.mapper

import com.andreibelous.plankdetektor.MainView
import com.andreibelous.plankdetektor.feature.PlankFeature

internal object NewsToViewAction : (PlankFeature.News) -> MainView.Action? {

    override fun invoke(news: PlankFeature.News): MainView.Action? =
        when (news) {
            is PlankFeature.News.Finished -> MainView.Action.ShowResultsDialog(news.data)
            is PlankFeature.News.Error -> MainView.Action.ShowError
        }
}