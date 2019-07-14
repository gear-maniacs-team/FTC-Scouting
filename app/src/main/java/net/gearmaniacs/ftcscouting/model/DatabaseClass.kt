package net.gearmaniacs.ftcscouting.model

import com.google.firebase.database.Exclude

abstract class DatabaseClass<T> : Comparable<T> {
    @Exclude
    @JvmField
    var key: String? = null
}
