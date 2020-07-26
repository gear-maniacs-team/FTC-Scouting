package net.gearmaniacs.core.model

abstract class DatabaseClass<T> : Comparable<T> {

    abstract var key: String
}
