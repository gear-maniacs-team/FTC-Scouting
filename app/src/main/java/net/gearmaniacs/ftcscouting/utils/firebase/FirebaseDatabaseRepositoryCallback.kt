package net.gearmaniacs.ftcscouting.utils.firebase

interface FirebaseDatabaseRepositoryCallback<T> {
    fun onSuccess(result: T)

    fun onError(e: Exception)
}
