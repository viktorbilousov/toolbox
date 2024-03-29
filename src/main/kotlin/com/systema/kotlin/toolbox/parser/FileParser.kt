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

    override fun createConfigurator(): FileParserConfigurator<E> = FileParserConfigurator<E>(parsers, skipper)
    
    init {
        installConfiguration()
    }

    override fun parse(string: String, strict: Boolean): List<E> {
        return parse(StringStreamReader(string), strict)
    }

    protected open fun ignoreParseError(e: Exception) : Boolean{ return false }

    override fun parse(reader: StringStreamReader, strict: Boolean): List<E> {
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
            @Suppress("UNCHECKED_CAST")
            try {
                val obj = parser.parser.parse(reader) as E
                list.add(obj)
            }
            catch (e: Exception) {
                // ok
                if(ignoreParseError(e)){
                    continue;
                }

                val msg = "Error By execution parser ${parser.name}, Start Block Line $lineCntBeforeStartParser"

                if (strict) {
                    throw FileParserException(msg, e)
                }
                else {
                    logger.log(Level.WARNING, msg, e)
                }
            }

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