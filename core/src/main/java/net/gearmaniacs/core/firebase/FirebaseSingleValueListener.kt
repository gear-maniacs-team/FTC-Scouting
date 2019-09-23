package net.gearmaniacs.core.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.model.DatabaseClass

class FirebaseSingleValueListener<T : DatabaseClass<T>>(
    private val clazz: Class<T>,
    private val liveData: MutexLiveData<List<T>>
) : ValueEventListener {

    override fun onDataChange(snapshot: DataSnapshot) {
        GlobalScope.launch(Dispatchers.IO) {
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