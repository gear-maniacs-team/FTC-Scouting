package net.gearmaniacs.ftcscouting.utils.architecture

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

inline val AndroidViewModel.app get() = getApplication<Application>()

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(): T {
    return ViewModelProviders.of(this)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProviders.of(this)[T::class.java]
}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(crossinline factory: () -> T): T {
    @Suppress("UNCHECKED_CAST")
    val vmFactory = object : ViewModelProvider.Factory {
        override fun <U : ViewModel> create(modelClass: Class<U>): U = factory() as U
    }

    return ViewModelProviders.of(this, vmFactory)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.getViewModel(crossinline factory: () -> T): T {
    @Suppress("UNCHECKED_CAST")
    val vmFactory = object : ViewModelProvider.Factory {
        override fun <U : ViewModel> create(modelClass: Class<U>): U = factory() as U
    }

    return ViewModelProviders.of(this, vmFactory)[T::class.java]
}

fun <T : Any, L : MutableLiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) {
    liveData.observe(this, Observer<T> { data: T? ->
        body(data)
    })
}

fun <T : Any, L : NonNullLiveData<T>> LifecycleOwner.observeNonNull(liveData: L, body: (T) -> Unit) {
    liveData.observe(this, Observer<T> { data: T? ->
        body(data ?: liveData.defaultValue)
    })
}