@file:Suppress("unused")

package org.vib.toolbox.builders

open class MapBuilder <K,V> (map: Map<K,V> = mapOf()) : MutableMap<K,V> by map.toMutableMap(), Builder<Map<K, V>> {
    infix fun K.to(obj: V) = this@MapBuilder.put(this, obj)
    override fun build() = this.toMap()
}

fun <K,V> mapBuilder(builder: MapBuilder<K, V>.() -> Unit) = MapBuilder<K, V>().apply(builder)
fun <K,V> mapBuilder(map: Map<K, V>, builder: MapBuilder<K, V>.() -> Unit) = MapBuilder(map).apply(builder)
fun <K,V> Map<K,V>.toMapBuilder(builder: MapBuilder<K, V>.() -> Unit) = MapBuilder(this).apply(builder)
