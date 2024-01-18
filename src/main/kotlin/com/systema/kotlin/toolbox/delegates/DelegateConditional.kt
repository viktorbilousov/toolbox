package com.systema.kotlin.toolbox.delegates

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty

public fun <T> updateWhen(needToUpdate: AtomicBoolean, updateFunction: () -> T) = DelegateUpdateIf<T>(needToUpdate, updateFunction)


@Suppress("UNCHECKED_CAST")
class DelegateUpdateIf<T>(private val needToUpdate: AtomicBoolean, private val updateFunction: () -> T) {

    private var value: T? = null

    operator fun getValue(any: Any?, property: KProperty<*>): T {
        if (value == null || needToUpdate.get()) {
            value = updateFunction.invoke()
            needToUpdate.set(false)
        }

        return value!!
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = value;
    }
}
