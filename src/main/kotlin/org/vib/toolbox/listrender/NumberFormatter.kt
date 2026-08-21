package org.vib.toolbox.listrender

fun interface NumberFormatter {
    fun invoke(number: Int): String
}