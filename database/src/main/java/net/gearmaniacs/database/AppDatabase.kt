package net.gearmaniacs.database

import androidx.room.Database
import androidx.room.RoomDatabase
import net.gearmaniacs.database.model.Tournament
import net.gearmaniacs.database.model.match.Match
import net.gearmaniacs.database.model.team.Team
import net.gearmaniacs.database.dao.MatchesDao
import net.gearmaniacs.database.dao.TeamsDao
import net.gearmaniacs.database.dao.TournamentsDao

@Database(version = 2, entities = [Tournament::class, Match::class, Team::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun tournamentsDao(): TournamentsDao

    abstract fun teamsDao(): TeamsDao

    abstract fun matchesDao(): MatchesDao
}
