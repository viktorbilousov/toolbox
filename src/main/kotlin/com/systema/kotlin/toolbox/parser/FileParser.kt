package com.systema.kotlin.toolbox.parser

import com.systema.kotlin.toolbox.Configurable
import com.systema.kotlin.toolbox.StringStreamReader
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("MemberVisibilityCanBePrivate")
open class FileParser<E>(builder: FileParserConfigurator<E>.() -> Unit) : Configurable<FileParserConfigurator<E>>(builder), IParser<List<E>> {

    private val logger: Logger = Logger.getLogger(this::class.java.getName())
    

    protected val parsers: MutableList<MatcherData> = mutableListOf()
    protected val skipper: MutableList<LineMatcher> = mutableListOf()
    protected var strict: Boolean = false
    private set;

    override fun createConfigurator(): FileParserConfigurator<E> = FileParserConfigurator<E>(parsers, skipper)
    
    init {
        installConfiguration()
    }

    override fun configure(config: FileParserConfigurator<E>) {
        super.configure(config)
        this.strict = config.strict;
    }

    protected open fun RunningFileParserContext.handleParsingException(e: Exception){
        val msg = "Error By execution parser ${parserName}, Start Block Line $lineNumberBeforeStartParser"

        if (strict) {
            throw FileParserException(msg, e)
        }
        else {
            logger.log(Level.WARNING, msg, e)
        }
    }

    protected open fun RunningFileParserContext.executeParser(parser: IParser<E>): E? {
        return parser.parse(reader)
    }

    protected fun RunningFileParserContext.executeParserOrThrow(parser: IParser<E>) : E?{
        try {
            return executeParser(parser)
        }
        catch (e: Exception) {
            handleParsingException(e)
        }
        return null;
    }

    final override fun parse(reader: StringStreamReader): List<E> {
        val list = mutableListOf<E>()
        val maxLineShift = parsers.maxOf { it.maxLineShift } + 1
        val linesBuffer: MutableList<String> = mutableListOf()
        while (reader.hasNext()) {
            val line = reader.readToLineBreak()
            var lineCnt = reader.currentLineCnt
            linesBuffer.add(0, line)
            if (linesBuffer.size > maxLineShift) {
                linesBuffer.removeLast()
            }

            if (skipper.any { it.match(line) }) {
                continue
            }

            val parser = parsers.filter { it.maxLineShift <= linesBuffer.lastIndex }.firstOrNull {
                it.lineIndex2Matchers.all {
                    val index = it.key
                    val matcher = it.value
                    val l = linesBuffer[index]
                    matcher.match(l)
                }
            } ?: continue

            while (reader.getPrev() in arrayOf('\n', '\r', ' ')) {
                reader.goBack()
            }

            reader.goBack(line.length)

            val lineCntBeforeStartParser = reader.currentLineCnt
            val context = RunningFileParserContextImpl(parser.name, strict, reader, lineCntBeforeStartParser, linesBuffer) {line}
            val res = context.executeParserOrThrow(parser.parser as IParser<E>) ?: continue
            list.add(res);
        }
        return list
    }

    fun read(files: List<File>): Map<File, List<E>> {

        val logger: Logger  = Logger.getLogger("FileParser")

        for (readFile in files) {
            require(readFile.exists()) {"File Not exists $readFile"}
        }

        var number = 1;
        val fileToSml = files.associateWith {
            logger.info("$number/${files.size} Read File ${it.absolutePath}")
            val text = it.readText()
            number++
            this.parse(text)
        }

        return fileToSml
    }
}