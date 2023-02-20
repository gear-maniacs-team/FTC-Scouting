package net.gearmaniacs.core.model

interface DatabaseClass<T> : Comparable<T> {
    val key: String

    fun copyWithKey(newKey: String): T
}
