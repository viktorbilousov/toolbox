package org.vib.toolbox

fun interface IFilter<T> {
    fun filter(obj: T) : Boolean
}