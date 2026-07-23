package com.sporti.core.domain.model

data class SportEvent(
    val id: String,
    val league: String,
    val sport: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val startTimeMillis: Long,
    val status: EventStatus,
    val venue: String?,
    val imageUrl: String?,
    val isFavorite: Boolean = false,
)

enum class EventStatus { UPCOMING, LIVE, FINISHED }

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val publishedAtMillis: Long,
    val url: String,
    val imageUrl: String?,
    val source: String,
    val isFavorite: Boolean = false,
)

data class Note(
    val id: Long = 0,
    val title: String,
    val body: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

enum class FavoriteType { MATCH, NEWS }

data class Favorite(
    val contentId: String,
    val type: FavoriteType,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val savedAtMillis: Long,
)

data class TrainingSession(
    val id: Long = 0,
    val durationMillis: Long,
    val lapsMillis: List<Long>,
    val createdAtMillis: Long,
)
