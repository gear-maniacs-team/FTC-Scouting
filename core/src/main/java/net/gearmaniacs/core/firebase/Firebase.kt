package net.gearmaniacs.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object Firebase {

    val database
        get() = FirebaseDatabase.getInstance()

    fun database(url: String) = FirebaseDatabase.getInstance(url)

    val auth
        get() = FirebaseAuth.getInstance()
}
