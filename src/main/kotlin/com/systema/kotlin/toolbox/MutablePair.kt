@file:Suppress("unused")

package com.systema.kotlin.toolbox

data class MutablePair<A,B>(var first: A, var second: B){
    fun toPair() : Pair<A,B> = Pair(first, second)

    constructor(pair: Pair<A,B>) : this(pair.first, pair.second)

    var left
        get() = first
        set(value) {first = value}
    var right
        get() = second
        set(value) {second = value}

}

fun <A, B> Pair<A,B>.toMutable() : MutablePair<A, B> = MutablePair(this)