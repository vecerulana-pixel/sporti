package com.sporti.core.data.repository

import com.sporti.core.data.local.EventDao
import com.sporti.core.data.local.EventEntity
import com.sporti.core.data.local.FavoriteDao
import com.sporti.core.data.local.FavoriteEntity
import com.sporti.core.data.local.NewsDao
import com.sporti.core.data.local.NewsEntity
import com.sporti.core.data.local.NoteDao
import com.sporti.core.data.local.NoteEntity
import com.sporti.core.data.local.TrainingDao
import com.sporti.core.data.local.TrainingSessionEntity
import com.sporti.core.data.remote.NewsRemoteDataSource
import com.sporti.core.data.remote.SportsRemoteDataSource
import com.sporti.core.domain.model.EventStatus
import com.sporti.core.domain.model.Favorite
import com.sporti.core.domain.model.FavoriteType
import com.sporti.core.domain.model.NewsArticle
import com.sporti.core.domain.model.Note
import com.sporti.core.domain.model.SportEvent
import com.sporti.core.domain.model.TrainingSession
import com.sporti.core.domain.repository.LibraryRepository
import com.sporti.core.domain.repository.SportsRepository
import com.sporti.core.domain.repository.TrainingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstSportsRepository @Inject constructor(
    private val eventDao: EventDao,
    private val newsDao: NewsDao,
    private val favoriteDao: FavoriteDao,
    private val sportsRemote: SportsRemoteDataSource,
    private val newsRemote: NewsRemoteDataSource,
) : SportsRepository {
    override fun observeEvents(): Flow<List<SportEvent>> = combine(
        eventDao.observeAll(),
        favoriteDao.observeAll(),
    ) { events, favorites ->
        val favoriteIds = favorites.filter { it.type == FavoriteType.MATCH.name }.mapTo(mutableSetOf()) { it.contentId }
        events.map { it.toDomain(it.id in favoriteIds) }
    }

    override fun observeNews(): Flow<List<NewsArticle>> = combine(
        newsDao.observeAll(),
        favoriteDao.observeAll(),
    ) { news, favorites ->
        val favoriteIds = favorites.filter { it.type == FavoriteType.NEWS.name }.mapTo(mutableSetOf()) { it.contentId }
        news.map { it.toDomain(it.id in favoriteIds) }
    }

    override suspend fun refresh(): Result<Unit> = coroutineScope {
        val events = async { runCatching { sportsRemote.fetchEvents() } }
        val news = async { runCatching { newsRemote.fetchNews() } }
        val eventResult = events.await()
        val newsResult = news.await()
        eventResult.getOrNull()?.takeIf { it.isNotEmpty() }?.let { eventDao.upsertAll(it) }
        newsResult.getOrNull()?.takeIf { it.isNotEmpty() }?.let { newsDao.upsertAll(it) }
        if (eventResult.isSuccess || newsResult.isSuccess) Result.success(Unit)
        else Result.failure(eventResult.exceptionOrNull() ?: newsResult.exceptionOrNull() ?: error("Refresh failed"))
    }

    override suspend fun toggleEventFavorite(event: SportEvent) {
        toggleFavorite(
            contentId = event.id,
            type = FavoriteType.MATCH,
            title = "${event.homeTeam} — ${event.awayTeam}",
            subtitle = event.league,
            imageUrl = event.imageUrl,
        )
    }

    override suspend fun toggleNewsFavorite(article: NewsArticle) {
        toggleFavorite(
            contentId = article.id,
            type = FavoriteType.NEWS,
            title = article.title,
            subtitle = article.source,
            imageUrl = article.imageUrl,
        )
    }

    private suspend fun toggleFavorite(
        contentId: String,
        type: FavoriteType,
        title: String,
        subtitle: String,
        imageUrl: String?,
    ) {
        if (favoriteDao.find(contentId, type.name) != null) {
            favoriteDao.delete(contentId, type.name)
        } else {
            favoriteDao.insert(
                FavoriteEntity(contentId, type.name, title, subtitle, imageUrl, System.currentTimeMillis()),
            )
        }
    }
}

@Singleton
class DefaultLibraryRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val favoriteDao: FavoriteDao,
) : LibraryRepository {
    override fun observeNotes(): Flow<List<Note>> = noteDao.observeAll().map { notes -> notes.map(NoteEntity::toDomain) }
    override fun observeFavorites(): Flow<List<Favorite>> = favoriteDao.observeAll().map { items -> items.map(FavoriteEntity::toDomain) }

    override suspend fun saveNote(note: Note) {
        val entity = NoteEntity(note.id, note.title.trim(), note.body.trim(), note.createdAtMillis, note.updatedAtMillis)
        if (note.id == 0L) noteDao.insert(entity) else noteDao.update(entity)
    }

    override suspend fun deleteNote(noteId: Long) = noteDao.delete(noteId)
    override suspend fun removeFavorite(contentId: String, type: FavoriteType) = favoriteDao.delete(contentId, type.name)
}

@Singleton
class DefaultTrainingRepository @Inject constructor(
    private val trainingDao: TrainingDao,
) : TrainingRepository {
    override fun observeSessions(): Flow<List<TrainingSession>> = trainingDao.observeAll().map { sessions ->
        sessions.map(TrainingSessionEntity::toDomain)
    }

    override suspend fun saveSession(durationMillis: Long, lapsMillis: List<Long>) {
        if (durationMillis < 1_000) return
        trainingDao.insert(
            TrainingSessionEntity(
                durationMillis = durationMillis,
                lapsCsv = lapsMillis.joinToString(","),
                createdAtMillis = System.currentTimeMillis(),
            ),
        )
    }
}

private fun EventEntity.toDomain(isFavorite: Boolean) = SportEvent(
    id, league, sport, homeTeam, awayTeam, homeScore, awayScore, startTimeMillis,
    runCatching { EventStatus.valueOf(status) }.getOrDefault(EventStatus.UPCOMING), venue, imageUrl, isFavorite,
)

private fun NewsEntity.toDomain(isFavorite: Boolean) = NewsArticle(
    id, title, summary, publishedAtMillis, url, imageUrl, source, isFavorite,
)

private fun NoteEntity.toDomain() = Note(id, title, body, createdAtMillis, updatedAtMillis)

private fun FavoriteEntity.toDomain() = Favorite(
    contentId,
    runCatching { FavoriteType.valueOf(type) }.getOrDefault(FavoriteType.NEWS),
    title,
    subtitle,
    imageUrl,
    savedAtMillis,
)

private fun TrainingSessionEntity.toDomain() = TrainingSession(
    id,
    durationMillis,
    lapsCsv.split(',').mapNotNull(String::toLongOrNull),
    createdAtMillis,
)
