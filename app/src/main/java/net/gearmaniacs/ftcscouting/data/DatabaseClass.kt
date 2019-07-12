package net.gearmaniacs.ftcscouting.data

import com.google.firebase.database.Exclude

abstract class DatabaseClass<T> : Comparable<T> {
    @Exclude
    @JvmField
    var key: String? = null
}
