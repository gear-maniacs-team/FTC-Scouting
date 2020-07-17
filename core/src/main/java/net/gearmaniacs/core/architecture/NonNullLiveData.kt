package net.gearmaniacs.core.architecture

import androidx.lifecycle.LiveData

@Suppress("RedundantOverride")
open class NonNullLiveData<T : Any>(val defaultValue: T) : LiveData<T>() {

    override fun getValue(): T = super.getValue() ?: defaultValue
}

open class MutableNonNullLiveData<T : Any>(defaultValue: T) : NonNullLiveData<T>(defaultValue) {
    public override fun setValue(value: T) {
        super.setValue(value)
    }

    public override fun postValue(value: T) {
        super.postValue(value)
    }
}
