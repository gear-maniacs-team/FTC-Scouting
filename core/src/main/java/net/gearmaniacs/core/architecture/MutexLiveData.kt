package net.gearmaniacs.core.architecture

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*

class MutexLiveData<T>(defaultValue: T) : NonNullLiveData<T>(defaultValue) {

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
        withContext(Dispatchers.Main) {
            value = newValue
        }
        mutex.unlock()
    }
}