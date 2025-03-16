package org.vib.toolbox

fun interface Matcher<T> {
    fun match(obj: T) : Boolean
}