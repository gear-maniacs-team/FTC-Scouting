package net.theluckycoder.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.gearmaniacs.core.model.Tournament

@Dao
abstract class TournamentsDao {

    @Query("SELECT * from skystone_tournament")
    abstract suspend fun getAll(): List<Tournament>

    @Query("SELECT * from skystone_tournament ORDER BY name ASC")
    abstract fun getAllFlow(): Flow<List<Tournament>>

    @Query("SELECT * from skystone_tournament WHERE `key` = :tournamentKey")
    abstract fun getFlow(tournamentKey: String): Flow<Tournament?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(tournament: Tournament)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(list: List<Tournament>)

    @Update
    abstract suspend fun update(tournament: Tournament)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun update(list: List<Tournament>)

    @Query("DELETE from skystone_tournament WHERE `key` = :tournamentKey")
    abstract suspend fun delete(tournamentKey: String)

    @Delete
    abstract suspend fun delete(list: List<Tournament>)

    @Query("DELETE from skystone_tournament")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(list: List<Tournament>) {
        // TODO: Find a way to improve this?
        val currentList = getAll()
        val currentKeysSet = currentList.mapTo(HashSet(currentList.size)) { it.key }

        val newKeysSet = list.mapTo(HashSet(list.size)) { it.key }

        val listToDelete = currentList.filterNot { newKeysSet.contains(it.key) }
        if (listToDelete.isNotEmpty())
            delete(listToDelete)

        val listToInsert = list.filterNot { currentKeysSet.contains(it.key) }
        if (listToInsert.isNotEmpty())
            insert(list)

        val commonKeys = currentKeysSet intersect newKeysSet
        val listToUpdate = list
            .filter { commonKeys.contains(it.key) }
            .filterNot { currentList.contains(it) }
        if (listToUpdate.isNotEmpty())
            update(listToUpdate)
    }
}
