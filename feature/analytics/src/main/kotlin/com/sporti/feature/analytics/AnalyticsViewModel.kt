package com.sporti.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sporti.core.domain.model.Favorite
import com.sporti.core.domain.model.Note
import com.sporti.core.domain.model.TrainingSession
import com.sporti.core.domain.repository.LibraryRepository
import com.sporti.core.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AnalyticsUiState(
    val sessionsCount: Int = 0,
    val totalTrainingMillis: Long = 0,
    val bestLapMillis: Long? = null,
    val notesCount: Int = 0,
    val favoritesCount: Int = 0,
    val weekDurationsMillis: List<Long> = List(7) { 0L },
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    trainingRepository: TrainingRepository,
    libraryRepository: LibraryRepository,
) : ViewModel() {
    val uiState: StateFlow<AnalyticsUiState> = combine(
        trainingRepository.observeSessions(),
        libraryRepository.observeNotes(),
        libraryRepository.observeFavorites(),
    ) { sessions: List<TrainingSession>, notes: List<Note>, favorites: List<Favorite> ->
        val today = LocalDate.now()
        val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        AnalyticsUiState(
            sessionsCount = sessions.size,
            totalTrainingMillis = sessions.sumOf(TrainingSession::durationMillis),
            bestLapMillis = sessions.flatMap(TrainingSession::lapsMillis).filter { it > 0 }.minOrNull(),
            notesCount = notes.size,
            favoritesCount = favorites.size,
            weekDurationsMillis = days.map { day ->
                sessions.filter { session ->
                    Instant.ofEpochMilli(session.createdAtMillis).atZone(ZoneId.systemDefault()).toLocalDate() == day
                }.sumOf(TrainingSession::durationMillis)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())
}
