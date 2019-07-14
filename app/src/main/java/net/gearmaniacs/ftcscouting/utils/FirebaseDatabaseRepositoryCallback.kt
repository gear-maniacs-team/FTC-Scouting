package net.gearmaniacs.ftcscouting.utils

interface FirebaseDatabaseRepositoryCallback<T> {
    fun onSuccess(result: T)

    fun onError(e: Exception)
}
