package com.systema.kotlin.toolbox.parser

import com.systema.kotlin.toolbox.StringStreamReader

interface RunningFileParserContext {
    val strict: Boolean
    val parserName: String
    val reader: StringStreamReader
    val line : String
    val currentLineNumber : Long
    val lineNumberBeforeStartParser : Long
    val lineBuffer: List<String>
}

open class RunningFileParserContextImpl internal constructor(
    override val parserName: String,
    override val strict: Boolean,
    override val reader: StringStreamReader,
    override val lineNumberBeforeStartParser: Long,
    override val lineBuffer: List<String>,
    private val currentLineProvider: () -> String,
): RunningFileParserContext{
    override val line: String get() =  currentLineProvider()
    override val currentLineNumber: Long get() = reader.currentLineCnt
}