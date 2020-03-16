package net.gearmaniacs.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

val Firebase.auth
    get() = FirebaseAuth.getInstance()