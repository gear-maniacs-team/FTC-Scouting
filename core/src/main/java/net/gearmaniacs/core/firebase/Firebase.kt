package net.gearmaniacs.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

@Suppress("unused")
val Firebase.auth
    get() = FirebaseAuth.getInstance()