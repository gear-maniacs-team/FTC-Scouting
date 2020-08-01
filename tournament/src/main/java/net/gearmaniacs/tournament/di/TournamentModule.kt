package net.gearmaniacs.tournament.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.ifLoggedIn
import net.gearmaniacs.tournament.repository.MatchesRepository
import net.gearmaniacs.tournament.repository.TeamsRepository
import net.gearmaniacs.tournament.repository.TournamentRepository
import net.theluckycoder.database.dao.MatchesDao
import net.theluckycoder.database.dao.TeamsDao
import net.theluckycoder.database.dao.TournamentsDao

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object TournamentModule {

    @Provides
    fun provideTournamentFirebaseReference() = Firebase.ifLoggedIn {
        Firebase.database
            .getReference(DatabasePaths.KEY_SKYSTONE)
            .child(it.uid)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideTournamentRepository(tournamentsDao: TournamentsDao, tournamentReference: DatabaseReference?) =
        TournamentRepository(tournamentsDao, tournamentReference)

    @Provides
    @ActivityRetainedScoped
    fun provideTeamsRepository(teamsDao: TeamsDao, tournamentReference: DatabaseReference?) =
        TeamsRepository(teamsDao, tournamentReference)

    @Provides
    @ActivityRetainedScoped
    fun provideMatchesRepository(matchesDao: MatchesDao, tournamentReference: DatabaseReference?) =
        MatchesRepository(matchesDao, tournamentReference)
}
