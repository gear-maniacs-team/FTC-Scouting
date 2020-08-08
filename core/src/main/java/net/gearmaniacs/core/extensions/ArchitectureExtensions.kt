package net.gearmaniacs.core.extensions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import net.gearmaniacs.core.architecture.NonNullLiveData

inline val AndroidViewModel.app get() = getApplication<Application>()

fun <T : Any?, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) {
    liveData.observe(this, { data: T? ->
        body(data)
    })
}

fun <T : Any?, L : NonNullLiveData<T>> LifecycleOwner.observeNonNull(
    liveData: L,
    body: (T) -> Unit
) {
    liveData.observe(this, { data: T? ->
        body(data ?: liveData.defaultValue)
    })
}
