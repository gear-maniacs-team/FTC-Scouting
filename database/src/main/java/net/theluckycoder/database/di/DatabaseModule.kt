package net.theluckycoder.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import net.theluckycoder.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext appContext: Context) =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providesTournamentsDao(database: AppDatabase) = database.tournamentsDao()

    @Provides
    fun providesTeamsDao(database: AppDatabase) = database.teamsDao()

    @Provides
    fun providesMatchesDao(database: AppDatabase) = database.matchesDao()
}