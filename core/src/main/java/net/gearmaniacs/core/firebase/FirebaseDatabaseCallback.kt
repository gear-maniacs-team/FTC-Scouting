package net.gearmaniacs.core.firebase

interface FirebaseDatabaseCallback<T> {
    fun onSuccess(result: T)

    fun onError(e: Exception)
}
