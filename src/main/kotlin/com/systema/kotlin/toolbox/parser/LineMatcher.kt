package com.systema.kotlin.toolbox.parser

fun interface LineMatcher {
    fun match(line: String) : Boolean
}