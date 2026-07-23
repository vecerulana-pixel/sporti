package com.sporti.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val league: String,
    val sport: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val startTimeMillis: Long,
    val status: String,
    val venue: String?,
    val imageUrl: String?,
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val publishedAtMillis: Long,
    val url: String,
    val imageUrl: String?,
    val source: String,
)

@Entity(tableName = "favorites", primaryKeys = ["contentId", "type"])
data class FavoriteEntity(
    val contentId: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val savedAtMillis: Long,
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMillis: Long,
    val lapsCsv: String,
    val createdAtMillis: Long,
)
