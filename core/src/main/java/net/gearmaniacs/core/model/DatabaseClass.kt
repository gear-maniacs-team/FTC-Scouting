package net.gearmaniacs.core.model

interface DatabaseClass<T> : Comparable<T> {
    var key: String
}
