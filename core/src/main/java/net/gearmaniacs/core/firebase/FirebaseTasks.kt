package net.gearmaniacs.core.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import net.gearmaniacs.core.model.DatabaseClass

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : DatabaseClass<T>> DatabaseReference.listValueEventListenerFlow(
    parser: (DataSnapshot) -> T?
) = callbackFlow<List<T>> {
    val eventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(error.toException())
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            val list = snapshot.children.asSequence()
                .filter { it.key != null }
                .map { parser(it) }
                .filterNotNull()
                .toMutableList()

            list.sort()

            channel.sendBlocking(list)
        }
    }

    this@listValueEventListenerFlow.addValueEventListener(eventListener)

    awaitClose {
        this@listValueEventListenerFlow.removeEventListener(eventListener)
    }
}

fun <T : DatabaseClass<T>> DatabaseReference.listValueEventListenerFlow(
    clazz: Class<T>
) = listValueEventListenerFlow { dataSnapshot ->
    // Try to parse the data to the destination class
    // If not null, add the key
    dataSnapshot.getValue(clazz)?.apply { this.key = dataSnapshot.key }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> DatabaseReference.valueEventListenerFlow(
    parser: (DataSnapshot) -> T?
) = callbackFlow<T?> {
    val eventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            channel.close(error.toException())
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            channel.sendBlocking(parser(snapshot))
        }
    }

    this@valueEventListenerFlow.addValueEventListener(eventListener)

    awaitClose {
        this@valueEventListenerFlow.removeEventListener(eventListener)
    }
}

inline fun <reified T> DatabaseReference.valueEventListenerFlow() =
    valueEventListenerFlow { dataSnapshot ->
        dataSnapshot.getValue<T>()
    }