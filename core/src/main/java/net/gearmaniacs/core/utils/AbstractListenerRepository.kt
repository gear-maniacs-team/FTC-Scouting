package net.gearmaniacs.core.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.justTry

abstract class AbstractListenerRepository {

    @Volatile
    private var listenerScope: CoroutineScope? = null

    @Volatile
    private var listenerCancellationJob: Job? = null

    suspend fun addListener(): Unit = coroutineScope {
        listenerCancellationJob?.cancel()
        listenerCancellationJob = null

        if (listenerScope != null) return@coroutineScope
        listenerScope = this

        onListenerAdded(this)
        Log.d(this@AbstractListenerRepository::class.simpleName, "Listener Added")
    }

    suspend fun removeListener(): Unit = coroutineScope {
        if (listenerScope == null) return@coroutineScope

        listenerCancellationJob?.cancel()
        listenerCancellationJob = launch {
            delay(REPO_TIMEOUT)
            ensureActive()
            Log.d(this@AbstractListenerRepository::class.simpleName, "Listener Removed")

            justTry { listenerScope?.cancel() }
            listenerScope = null
            onListenerRemoved()
        }
    }

    suspend fun clear() = coroutineScope {
        Log.d(this@AbstractListenerRepository::class.simpleName, "Listener Cleared")
        justTry { listenerScope?.cancel() }
        listenerScope = null

        listenerCancellationJob?.cancel()
        listenerCancellationJob = null

        onListenerRemoved()
    }

    protected abstract suspend fun onListenerAdded(scope: CoroutineScope)

    protected open suspend fun onListenerRemoved() = Unit

    companion object {
        const val REPO_TIMEOUT = 5000L
    }
}
