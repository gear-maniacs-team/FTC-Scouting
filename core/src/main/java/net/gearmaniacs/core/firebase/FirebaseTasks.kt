package net.gearmaniacs.core.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import net.gearmaniacs.core.model.DatabaseClass
import kotlin.reflect.KClass

fun <T : DatabaseClass<T>> DatabaseReference.listValueEventFlow(
    parser: (DataSnapshot) -> T?
) = callbackFlow {
    val eventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            error.toException().printStackTrace()
            channel.close()
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            val list = snapshot.children.asSequence()
                .filter { it.key != null }
                .mapNotNull { parser(it) }
                .toList()

            channel.trySendBlocking(list)
        }
    }

    this@listValueEventFlow.addValueEventListener(eventListener)

    awaitClose {
        this@listValueEventFlow.removeEventListener(eventListener)
    }
}

fun <T : DatabaseClass<T>> DatabaseReference.listValueEventFlow(
    clazz: KClass<T>
) = listValueEventFlow { dataSnapshot ->
    // Try to parse the data to the destination class
    // If not null, add the key
    val snapshotKey = dataSnapshot.key
    val value = dataSnapshot.getValue(clazz.java)

    if (snapshotKey != null && value != null)
        value.also { it.key = snapshotKey }
    else
        null
}

fun <T> DatabaseReference.valueEventFlow(
    parser: (DataSnapshot) -> T?
) = callbackFlow {
    val eventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            error.toException().printStackTrace()
            channel.close()
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            channel.trySendBlocking(parser(snapshot))
        }
    }

    this@valueEventFlow.addValueEventListener(eventListener)

    awaitClose {
        this@valueEventFlow.removeEventListener(eventListener)
    }
}

inline fun <reified T> DatabaseReference.valueEventFlow() =
    valueEventFlow { dataSnapshot ->
        dataSnapshot.getValue<T>()
    }
