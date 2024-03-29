package com.systema.kotlin.toolbox.parser

data class MatcherData(
        val lineIndex2Matchers: Map<Int, LineMatcher>,
        val parser: IParser<*>,
        val name: String = defaultParserId(parser)
    ) {

    companion object {
        private val nextParsersId: MutableMap<String, Int> = mutableMapOf()

        internal fun defaultParserId(parser: IParser<*>): String {
            val name = parser::class.java.simpleName
            val id = nextParsersId[name] ?: 1
            nextParsersId[name] = id + 1
            return "$name--$id"
        }
    }

    val maxLineShift get() = lineIndex2Matchers.keys.maxOf { it }
    val minLineShift get() = lineIndex2Matchers.keys.minOf { it }
}