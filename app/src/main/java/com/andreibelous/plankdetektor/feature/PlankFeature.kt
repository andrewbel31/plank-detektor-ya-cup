package com.andreibelous.plankdetektor.feature

import com.andreibelous.plankdetektor.PlankDataSource
import com.andreibelous.plankdetektor.cast
import com.andreibelous.plankdetektor.feature.PlankFeature.News
import com.andreibelous.plankdetektor.feature.PlankFeature.Wish
import com.andreibelous.plankdetektor.feature.data.Attempt
import com.andreibelous.plankdetektor.feature.data.AttemptsDataSource
import com.andreibelous.plankdetektor.toObservable
import com.badoo.mvicore.element.*
import com.badoo.mvicore.feature.BaseFeature
import com.badoo.mvicore.feature.Feature
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class PlankFeature(
    private val dataSource: AttemptsDataSource
) : Feature<Wish, PlankState, News> by BaseFeature(
    initialState = PlankState(),
    wishToAction = Action::ExecuteWish,
    actor = ActorImpl(dataSource),
    postProcessor = PostProcessorImpl,
    reducer = ReducerImpl(),
    bootstrapper = BootstrapperImpl,
    newsPublisher = NewsPublisherImpl()
) {

    sealed interface Wish {

        object ShowResults : Wish
    }

    sealed interface News {

        data class Finished(val data: List<Attempt>) : News
        object Error : News
    }

    private sealed interface Action {

        data class ExecuteWish(val wish: Wish) : Action
        data class PlankInProgress(val ts: Long) : Action
        data class NoPlank(val ts: Long) : Action
        object ShowResults : Action
    }

    private sealed interface Effect {

        data class PlankStarted(val ts: Long) : Effect
        data class PlankFinished(val finishTs: Long) : Effect
        data class AttemptsLoaded(val attempts: List<Attempt>) : Effect
    }

    private class ActorImpl(
        private val dataSource: AttemptsDataSource
    ) : Actor<PlankState, Action, Effect> {

        override fun invoke(state: PlankState, action: Action): Observable<out Effect> =
            when (action) {
                is Action.PlankInProgress ->
                    state.stage.toObservable()
                        .filter { it is Stage.Init }
                        .map { Effect.PlankStarted(action.ts) }
                is Action.NoPlank ->
                    state.stage.toObservable()
                        .filter { it is Stage.InProgress }
                        .map { it.cast<Stage.InProgress>() }
                        .concatMap {
                            dataSource.saveAttempt(
                                Attempt(
                                    date = System.currentTimeMillis(),
                                    start = it.startTs,
                                    end = action.ts
                                )
                            ).andThen(Effect.PlankFinished(action.ts).toObservable())
                        }

                is Action.ShowResults -> loadResults()
                is Action.ExecuteWish -> executeWish(action.wish)
            }.observeOn(AndroidSchedulers.mainThread())

        private fun executeWish(wish: Wish): Observable<Effect> =
            when (wish) {
                is Wish.ShowResults -> loadResults()

            }

        private fun loadResults(): Observable<Effect> =
            dataSource
                .loadAttempts()
                .map { Effect.AttemptsLoaded(it) }

    }

    private class ReducerImpl : Reducer<PlankState, Effect> {

        override fun invoke(state: PlankState, effect: Effect): PlankState =
            when (effect) {
                is Effect.AttemptsLoaded -> state
                is Effect.PlankStarted -> state.copy(stage = Stage.InProgress(effect.ts))
                is Effect.PlankFinished -> state.copy(stage = Stage.Init)
            }
    }

    private object PostProcessorImpl : PostProcessor<Action, Effect, PlankState> {

        override fun invoke(action: Action, effect: Effect, state: PlankState): Action? =
            when (effect) {
                is Effect.PlankFinished -> Action.ShowResults
                else -> null
            }
    }

    private class NewsPublisherImpl : NewsPublisher<Action, Effect, PlankState, News> {

        override fun invoke(action: Action, effect: Effect, state: PlankState): News? =
            when (effect) {
                is Effect.AttemptsLoaded -> News.Finished(effect.attempts)
                else -> null
            }
    }

    private object BootstrapperImpl : Bootstrapper<Action> {

        private data class State(
            val isPlank: Boolean,
            val timestamp: Long
        )

        private var buffer = mutableListOf<State>()

        override fun invoke(): Observable<Action> =
            Observable
                .interval(CHECK_INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap {
                    if (buffer.size < 20) {
                        buffer.add(
                            State(
                                isPlank = PlankDataSource.isPlank,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        Observable.empty()
                    } else {
                        buffer.removeAt(0)
                        State(
                            isPlank = PlankDataSource.isPlank,
                            timestamp = System.currentTimeMillis()
                        )

                        if (buffer.count { it.isPlank } > 12) {
                            Action
                                .PlankInProgress(buffer.first { it.isPlank }.timestamp)
                                .toObservable()
                        } else {
                            Action
                                .NoPlank(buffer.first { !it.isPlank }.timestamp)
                                .toObservable()
                        }
                    }
                }
    }

    private companion object {

        private const val CHECK_INTERVAL = 50L
    }
}