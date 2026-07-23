package com.sporti.core.data.di

import android.content.Context
import androidx.room.Room
import com.sporti.core.data.local.EventDao
import com.sporti.core.data.local.FavoriteDao
import com.sporti.core.data.local.NewsDao
import com.sporti.core.data.local.NoteDao
import com.sporti.core.data.local.SportiDatabase
import com.sporti.core.data.local.TrainingDao
import com.sporti.core.data.remote.SportsApi
import com.sporti.core.data.repository.DefaultLibraryRepository
import com.sporti.core.data.repository.DefaultTrainingRepository
import com.sporti.core.data.repository.OfflineFirstSportsRepository
import com.sporti.core.domain.repository.LibraryRepository
import com.sporti.core.domain.repository.SportsRepository
import com.sporti.core.domain.repository.TrainingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindSports(impl: OfflineFirstSportsRepository): SportsRepository
    @Binds @Singleton abstract fun bindLibrary(impl: DefaultLibraryRepository): LibraryRepository
    @Binds @Singleton abstract fun bindTraining(impl: DefaultTrainingRepository): TrainingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SportiDatabase =
        Room.databaseBuilder(context, SportiDatabase::class.java, "sporti.db").build()

    @Provides fun provideEventDao(db: SportiDatabase): EventDao = db.eventDao()
    @Provides fun provideNewsDao(db: SportiDatabase): NewsDao = db.newsDao()
    @Provides fun provideFavoriteDao(db: SportiDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideNoteDao(db: SportiDatabase): NoteDao = db.noteDao()
    @Provides fun provideTrainingDao(db: SportiDatabase): TrainingDao = db.trainingDao()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    @Provides @Singleton
    fun provideSportsApi(client: OkHttpClient): SportsApi = Retrofit.Builder()
        .baseUrl("https://www.thesportsdb.com/api/v1/json/123/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SportsApi::class.java)
}
