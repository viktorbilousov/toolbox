@file:Suppress("unused")

package com.systema.kotlin.toolbox.delegates

import kotlin.reflect.KProperty

inline fun <reified T> valueToMap(map: MutableMap<String, Any?>, defaultValue: T? = null, name: String? = null) =
    DelegateToMap(map, defaultValue, name)

inline fun <reified T> notNullValueToMap(map: MutableMap<String, Any?>, defaultValue: T? = null, name: String? = null) =
    NotNullDelegateToMap(map, defaultValue, name)


@Suppress("UNCHECKED_CAST")
class DelegateToMap<T>(private val map: MutableMap<String, Any?>, private val defaultValue: T? = null, private val name: String? = null) {

    private fun findKey(name: String): String? {
        return map.keys.firstOrNull { it.equals(name, true) }
    }

    private fun propName(property: KProperty<*>) = name ?: property.name

    operator fun getValue(any: Any, property: KProperty<*>): T? {
        val key = findKey(propName(property))
        if (key == null) {
            setValue(this, property, defaultValue)
        }
        return map[key] as? T
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        val key = name ?: property.name.uppercase()
        map[key] = value
    }
}

@Suppress("UNCHECKED_CAST")
class NotNullDelegateToMap<T>(private val map: MutableMap<String, Any?>, private val defaultValue: T? = null, private val name: String? = null) {

    private fun findKey(name: String): String? {
        return map.keys.firstOrNull { it.equals(name, true) }
    }

    private fun propName(property: KProperty<*>) = name ?: property.name

    @Suppress("FoldInitializerAndIfToElvis")
    operator fun getValue(any: Any, property: KProperty<*>): T {
        val key = findKey(name ?: property.name)
        if (key == null) {
            if (defaultValue != null) {
                setValue(this, property, defaultValue)
                return getValue(any, property)
            } else {
                throw IllegalAccessException("Property ${propName(property)} not found in map!")
            }
        }
        return map[key]!! as T
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = name ?: property.name.uppercase()
        map[key] = value
    }
}