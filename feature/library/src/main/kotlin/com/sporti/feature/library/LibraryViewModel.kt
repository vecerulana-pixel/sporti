package com.sporti.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sporti.core.domain.model.Favorite
import com.sporti.core.domain.model.Note
import com.sporti.core.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val notes: List<Note> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
) : ViewModel() {
    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeNotes(),
        repository.observeFavorites(),
    ) { notes, favorites -> LibraryUiState(notes, favorites) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun saveNote(existing: Note?, title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.saveNote(
                Note(
                    id = existing?.id ?: 0,
                    title = title,
                    body = body,
                    createdAtMillis = existing?.createdAtMillis ?: now,
                    updatedAtMillis = now,
                ),
            )
        }
    }

    fun deleteNote(note: Note) = viewModelScope.launch { repository.deleteNote(note.id) }
    fun removeFavorite(favorite: Favorite) = viewModelScope.launch { repository.removeFavorite(favorite.contentId, favorite.type) }
}
