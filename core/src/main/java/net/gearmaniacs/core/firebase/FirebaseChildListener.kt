package net.gearmaniacs.core.firebase

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.architecture.MutexLiveData
import net.gearmaniacs.core.model.DatabaseClass

class FirebaseChildListener<T : DatabaseClass<T>>(
    private val clazz: Class<T>,
    private val liveData: MutexLiveData<List<T>>
) : ChildEventListener {

    override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
        val key = snapshot.key ?: return

        GlobalScope.launch(Dispatchers.Default) {
            val item = snapshot.getValue(clazz)!!.apply { this.key = key }
            val list = liveData.getValueAndLock().toMutableList()

            if (!list.any { it == item }) {
                list.add(item)
                list.sort()
                liveData.postValueAndUnlock(list)
            } else {
                liveData.unlock()
            }
        }
    }

    override fun onChildMoved(snapshot: DataSnapshot, p1: String?) = Unit

    override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
        val key = snapshot.key ?: return

        GlobalScope.launch(Dispatchers.Default) {
            val item = snapshot.getValue(clazz)!!.apply { this.key = key }
            val list = liveData.getValueAndLock().toMutableList()
            val index = list.indexOfFirst { it.key == key }

            if (index == -1 || index >= list.size)
                list.add(item)
            else
                list[index] = item

            list.sort()
            liveData.postValueAndUnlock(list)
        }
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
        val key = snapshot.key ?: return

        GlobalScope.launch(Dispatchers.Default) {
            val item = snapshot.getValue(clazz)!!.apply { this.key = key }
            val list = liveData.getValueAndLock().toMutableList()

            list.remove(item)
            liveData.postValueAndUnlock(list)
        }
    }

    override fun onCancelled(error: DatabaseError) = Unit
}
