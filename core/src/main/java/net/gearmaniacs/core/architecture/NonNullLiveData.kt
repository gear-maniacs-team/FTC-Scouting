package net.gearmaniacs.core.architecture

import androidx.lifecycle.MutableLiveData

@Suppress("RedundantOverride")
open class NonNullLiveData<T : Any>(val defaultValue: T) : MutableLiveData<T>() {

    override fun getValue(): T = super.getValue() ?: defaultValue

    override fun setValue(value: T) {
        super.setValue(value)
    }

    override fun postValue(value: T) {
        super.postValue(value)
    }
}
