package com.systema.kotlin.toolbox

interface ISerializer<E> {
    fun print(list: E) : String
}