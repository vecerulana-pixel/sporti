package com.sporti.core.domain.repository

import com.sporti.core.domain.model.Favorite
import com.sporti.core.domain.model.FavoriteType
import com.sporti.core.domain.model.NewsArticle
import com.sporti.core.domain.model.Note
import com.sporti.core.domain.model.SportEvent
import com.sporti.core.domain.model.TrainingSession
import kotlinx.coroutines.flow.Flow

interface SportsRepository {
    fun observeEvents(): Flow<List<SportEvent>>
    fun observeNews(): Flow<List<NewsArticle>>
    suspend fun refresh(): Result<Unit>
    suspend fun toggleEventFavorite(event: SportEvent)
    suspend fun toggleNewsFavorite(article: NewsArticle)
}

interface LibraryRepository {
    fun observeNotes(): Flow<List<Note>>
    fun observeFavorites(): Flow<List<Favorite>>
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(noteId: Long)
    suspend fun removeFavorite(contentId: String, type: FavoriteType)
}

interface TrainingRepository {
    fun observeSessions(): Flow<List<TrainingSession>>
    suspend fun saveSession(durationMillis: Long, lapsMillis: List<Long>)
}
