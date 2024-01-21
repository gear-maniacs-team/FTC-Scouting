package net.gearmaniacs.core.firebase

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.core.utilities.PushIdGenerator
import com.google.firebase.ktx.Firebase

@Suppress("UnusedReceiverParameter")
val Firebase.isLoggedIn
    get() = Firebase.auth.currentUser != null

inline fun <R> Firebase.ifLoggedIn(func: (user: FirebaseUser) -> R): R? {
    auth.currentUser?.let { user ->
        return func(user)
    }

    return null
}

@Suppress("UnusedReceiverParameter")
@SuppressLint("RestrictedApi")
fun Firebase.generatePushId(): String =
    PushIdGenerator.generatePushChildName(System.currentTimeMillis())
