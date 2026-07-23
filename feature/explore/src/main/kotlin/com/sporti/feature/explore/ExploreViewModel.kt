package com.sporti.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sporti.core.domain.model.NewsArticle
import com.sporti.core.domain.model.SportEvent
import com.sporti.core.domain.repository.SportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExploreUiState(
    val events: List<SportEvent> = emptyList(),
    val news: List<NewsArticle> = emptyList(),
    val selectedLeague: String? = null,
    val isRefreshing: Boolean = false,
    val hasRefreshError: Boolean = false,
) {
    val leagues: List<String> get() = events.map { it.league }.filter(String::isNotBlank).distinct()
    val visibleEvents: List<SportEvent> get() = selectedLeague?.let { league -> events.filter { it.league == league } } ?: events
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: SportsRepository,
) : ViewModel() {
    private val selectedLeague = MutableStateFlow<String?>(null)
    private val refresh = MutableStateFlow(false to false)

    val uiState: StateFlow<ExploreUiState> = combine(
        repository.observeEvents(),
        repository.observeNews(),
        selectedLeague,
        refresh,
    ) { events, news, league, refreshState ->
        ExploreUiState(events, news, league, refreshState.first, refreshState.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun selectLeague(league: String?) { selectedLeague.value = league }

    fun refresh() {
        if (refresh.value.first) return
        viewModelScope.launch {
            refresh.value = true to false
            repository.refresh().fold(
                onSuccess = { refresh.value = false to false },
                onFailure = { refresh.value = false to true },
            )
        }
    }

    fun toggleFavorite(event: SportEvent) = viewModelScope.launch { repository.toggleEventFavorite(event) }
    fun toggleFavorite(article: NewsArticle) = viewModelScope.launch { repository.toggleNewsFavorite(article) }
}
