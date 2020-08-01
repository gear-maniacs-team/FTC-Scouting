package net.gearmaniacs.ftcscouting.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.ftcscouting.repository.MainRepository
import net.theluckycoder.database.dao.TournamentsDao

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainModule {

    @Provides
    @ActivityRetainedScoped
    fun provideMainRepository(tournamentsDao: TournamentsDao, appPreferences: AppPreferences) =
        MainRepository(tournamentsDao, appPreferences)
}
