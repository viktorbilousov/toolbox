package com.systema.kotlin.toolbox.parser

import com.systema.kotlin.toolbox.parser.MatcherData.Companion.defaultParserId

open class FileParserConfigurator<E> (
    val parsers: MutableList<MatcherData>,
    private val skipper: MutableList<LineMatcher>,
    ) {

    constructor(configurator: FileParserConfigurator<E>) : this(configurator.parsers, configurator.skipper)

    var strict: Boolean = false

    class FileParserBuilderPipeline<E>(
        val parser: IParser<out E>, private val parsers: MutableList<MatcherData>, internal val id: String
    ) {
        internal val map = mutableMapOf<Int, LineMatcher>()
    }


    infix fun FileParserBuilderPipeline<E>.whenLine(matcher: LineMatcher): FileParserBuilderPipeline<E> = apply {
        map[0] = matcher
        if (parsers.none { it.name == id }) {
            parsers.add(MatcherData(map, parser, id))
        }
    }


    fun FileParserBuilderPipeline<E>.whenPrevNLine(line: Int, matcher: LineMatcher): FileParserBuilderPipeline<E> =
        apply {
            require(line >= 0)
            map[line] = matcher
            if (parsers.none { it.name == id }) {
                parsers.add(MatcherData(map, parser, id))
            }
        }

    infix fun FileParserBuilderPipeline<E>.whenPreviousLine(matcher: LineMatcher): FileParserBuilderPipeline<E> =
        apply {
            val id = defaultParserId(parser)
            map[1] = matcher
            if (parsers.none { it.name == id }) {
                parsers.add(MatcherData(map, parser, id))
            }
        }

    fun skipLineWhen(matcher: LineMatcher) {
        skipper.add(matcher)
    }

    fun useParser(parser: IParser<out E>, name: String = defaultParserId(parser)): FileParserBuilderPipeline<E> =
        FileParserBuilderPipeline(parser, parsers, name)
}
    