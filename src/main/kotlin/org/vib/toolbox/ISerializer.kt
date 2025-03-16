package org.vib.toolbox

interface ISerializer<E> {
    fun print(list: E) : String
}