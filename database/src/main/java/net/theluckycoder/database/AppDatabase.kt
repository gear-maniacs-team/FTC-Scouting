package net.theluckycoder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.Tournament
import net.theluckycoder.database.dao.MatchesDao
import net.theluckycoder.database.dao.TeamsDao
import net.theluckycoder.database.dao.TournamentDao

@Database(version = 1, entities = [Tournament::class, Match::class, Team::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun tournamentsDao(): TournamentDao

    abstract fun teamsDao(): TeamsDao

    abstract fun matchesDao(): MatchesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
