@file:Suppress("unused")

package org.vib.toolbox.builders

class ListBuilder <E> (list: List<E>) : MutableList<E> by list.toMutableList(), Builder<List<E>> {
    fun apply(list: Collection<E>)  = apply {
        this.addAll(list)
    }

    fun apply(vararg el: E) = apply {
        this.addAll(el)
    }
    fun applyFromBeginning(vararg  el: E) = apply { el.reversed().forEach { this@ListBuilder.add(0, it) } }
    fun applyFromBeginning(list: List<E>) = apply { list.reversed().forEach { this@ListBuilder.add(0, it) } }

    override fun build(): List<E> {
        return this
    }
}

fun <E, C: Collection<E>> C.toListBuilder() = ListBuilder(this.toList())

fun <E> listBuilderOf(vararg el: E) = ListBuilder(el.toMutableList())
