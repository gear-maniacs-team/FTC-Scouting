package net.gearmaniacs.ftcscouting.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import net.gearmaniacs.ftcscouting.ui.activity.MainActivity
import net.gearmaniacs.login.ui.activity.LoginActivity

@Module
@InstallIn(ApplicationComponent::class)
object LoginModule {

    @Provides
    fun providesMainActivityClass() =
        LoginActivity.MainActivityClass(MainActivity::class.java.canonicalName!!)
}
