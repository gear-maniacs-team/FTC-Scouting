package net.theluckycoder.database

import androidx.room.Database
import androidx.room.RoomDatabase
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.team.Team
import net.theluckycoder.database.dao.MatchesDao
import net.theluckycoder.database.dao.TeamsDao
import net.theluckycoder.database.dao.TournamentsDao

@Database(version = 1, entities = [Tournament::class, Match::class, Team::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun tournamentsDao(): TournamentsDao

    abstract fun teamsDao(): TeamsDao

    abstract fun matchesDao(): MatchesDao
}
