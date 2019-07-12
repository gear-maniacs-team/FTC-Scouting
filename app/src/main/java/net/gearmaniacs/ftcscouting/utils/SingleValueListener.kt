package net.gearmaniacs.ftcscouting.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import net.gearmaniacs.ftcscouting.data.DatabaseClass
import net.gearmaniacs.ftcscouting.utils.architecture.MutexLiveData

class SingleValueListener<T : DatabaseClass<T>>(
    private val clazz: Class<T>,
    private val liveData: MutexLiveData<List<T>>,
    private val coroutineScope: CoroutineScope
) : ValueEventListener {

    override fun onDataChange(snapshot: DataSnapshot) {
        coroutineScope.launch(Dispatchers.Default) {
            liveData.lock()

            val list = snapshot.children.asSequence()
                .filter { it.key != null }
                .map {
                    // Try to parse the data to the destination class
                    // If not null, add the key
                    it.getValue(clazz)?.apply { this.key = it.key }
                }
                .filterNotNull()
                .toMutableList()

            list.sort()
            liveData.postValueAndUnlock(list)
        }
    }

    override fun onCancelled(error: DatabaseError) = Unit
}