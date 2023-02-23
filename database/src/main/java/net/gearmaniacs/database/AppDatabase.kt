package net.gearmaniacs.database

import androidx.room.Database
import androidx.room.RoomDatabase
import net.gearmaniacs.core.model.Tournament
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.database.dao.MatchesDao
import net.gearmaniacs.database.dao.TeamsDao
import net.gearmaniacs.database.dao.TournamentsDao

@Database(version = 1, entities = [Tournament::class, Match::class, Team::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun tournamentsDao(): TournamentsDao

    abstract fun teamsDao(): TeamsDao

    abstract fun matchesDao(): MatchesDao
}
