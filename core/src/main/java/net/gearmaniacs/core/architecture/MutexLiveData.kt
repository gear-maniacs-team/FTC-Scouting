package net.gearmaniacs.core.architecture

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

class MutexLiveData<T : Any>(defaultValue: T) : NonNullLiveData<T>(defaultValue) {

    private val mutex = Mutex()

    suspend fun getValueAndLock(): T {
        mutex.lock()
        return value
    }

    fun setValueAndUnlock(newValue: T) {
        value = newValue
        mutex.unlock()
    }

    suspend fun lock() = mutex.lock()

    fun unlock() = mutex.unlock()

    suspend fun postValueAndUnlock(newValue: T) = coroutineScope {
        withContext(Dispatchers.Main.immediate) {
            value = newValue
        }
        mutex.unlock()
    }
}