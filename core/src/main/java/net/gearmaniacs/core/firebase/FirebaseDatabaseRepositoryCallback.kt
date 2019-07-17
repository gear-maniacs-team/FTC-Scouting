package net.gearmaniacs.core.firebase

interface FirebaseDatabaseRepositoryCallback<T> {
    fun onSuccess(result: T)

    fun onError(e: Exception)
}
