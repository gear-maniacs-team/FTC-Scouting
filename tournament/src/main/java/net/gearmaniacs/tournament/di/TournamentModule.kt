package net.gearmaniacs.tournament.di

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.ifLoggedIn

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object TournamentModule {

    @Provides
    fun provideTournamentFirebaseReference() = Firebase.ifLoggedIn {
        Firebase.database
            .getReference(DatabasePaths.KEY_UNIVERSAL)
            .child(it.uid)
    }
}
