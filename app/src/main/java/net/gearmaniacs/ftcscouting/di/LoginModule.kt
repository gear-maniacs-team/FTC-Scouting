package net.gearmaniacs.ftcscouting.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import net.gearmaniacs.ftcscouting.ui.activity.IntroActivity
import net.gearmaniacs.ftcscouting.ui.activity.MainActivity
import net.gearmaniacs.login.ui.activity.LoginActivity
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object LoginModule {

    @Provides
    @Singleton
    fun providesMainActivityClass() =
        LoginActivity.MainActivityClass(MainActivity::class.java.canonicalName!!)

    @Provides
    @Singleton
    fun providesIntroActivityClass() =
        LoginActivity.IntroActivityClass(IntroActivity::class.java.canonicalName!!)
}
