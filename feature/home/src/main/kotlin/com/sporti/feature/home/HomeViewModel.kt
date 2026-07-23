package com.sporti.feature.home

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sporti.core.domain.model.NewsArticle
import com.sporti.core.domain.model.SportEvent
import com.sporti.core.domain.repository.SportsRepository
import com.sporti.core.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StopwatchState(
    val elapsedMillis: Long = 0,
    val isRunning: Boolean = false,
    val lapsMillis: List<Long> = emptyList(),
)

data class HomeUiState(
    val events: List<SportEvent> = emptyList(),
    val news: List<NewsArticle> = emptyList(),
    val isRefreshing: Boolean = true,
    val hasRefreshError: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sportsRepository: SportsRepository,
    private val trainingRepository: TrainingRepository,
) : ViewModel() {
    private val refreshState = MutableStateFlow(true to false)
    private val _stopwatch = MutableStateFlow(StopwatchState())
    val stopwatch: StateFlow<StopwatchState> = _stopwatch
    private var ticker: Job? = null
    private var startedAt = 0L
    private var elapsedBeforeStart = 0L

    val uiState: StateFlow<HomeUiState> = combine(
        sportsRepository.observeEvents(),
        sportsRepository.observeNews(),
        refreshState,
    ) { events, news, refresh ->
        HomeUiState(events.take(4), news.take(3), refresh.first, refresh.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init { refresh() }

    fun refresh() {
        if (refreshState.value.first && uiState.value.events.isNotEmpty()) return
        viewModelScope.launch {
            refreshState.value = true to false
            sportsRepository.refresh().fold(
                onSuccess = { refreshState.value = false to false },
                onFailure = { refreshState.value = false to true },
            )
        }
    }

    fun toggleStopwatch() {
        if (_stopwatch.value.isRunning) pause() else start()
    }

    private fun start() {
        startedAt = SystemClock.elapsedRealtime()
        elapsedBeforeStart = _stopwatch.value.elapsedMillis
        _stopwatch.update { it.copy(isRunning = true) }
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (true) {
                _stopwatch.update { state ->
                    state.copy(elapsedMillis = elapsedBeforeStart + SystemClock.elapsedRealtime() - startedAt)
                }
                delay(16)
            }
        }
    }

    private fun pause() {
        ticker?.cancel()
        val elapsed = elapsedBeforeStart + SystemClock.elapsedRealtime() - startedAt
        _stopwatch.update { it.copy(elapsedMillis = elapsed, isRunning = false) }
    }

    fun addLap() {
        val state = _stopwatch.value
        if (!state.isRunning || state.elapsedMillis == 0L) return
        val previous = state.lapsMillis.sum()
        _stopwatch.update { it.copy(lapsMillis = it.lapsMillis + (it.elapsedMillis - previous).coerceAtLeast(0)) }
    }

    fun reset() {
        val snapshot = _stopwatch.value
        ticker?.cancel()
        _stopwatch.value = StopwatchState()
        elapsedBeforeStart = 0
        if (snapshot.elapsedMillis >= 1_000) {
            viewModelScope.launch { trainingRepository.saveSession(snapshot.elapsedMillis, snapshot.lapsMillis) }
        }
    }
}
