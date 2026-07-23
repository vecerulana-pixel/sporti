package com.sporti.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        EventEntity::class,
        NewsEntity::class,
        FavoriteEntity::class,
        NoteEntity::class,
        TrainingSessionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SportiDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun newsDao(): NewsDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun noteDao(): NoteDao
    abstract fun trainingDao(): TrainingDao
}
