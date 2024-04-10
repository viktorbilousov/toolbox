package com.systema.kotlin.toolbox

fun interface Matcher<T> {
    fun match(obj: T) : Boolean
}