package org.vib.toolbox.builders

interface Builder<T> {
    fun build() : T
}