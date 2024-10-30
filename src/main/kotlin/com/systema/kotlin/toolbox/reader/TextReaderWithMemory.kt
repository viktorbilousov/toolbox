package com.systema.kotlin.toolbox.reader

import java.io.BufferedReader
import java.io.Reader
import java.io.StringReader

open class TextReaderWithMemory: ReaderWithMemory {
    constructor(reader: Reader, bufferLen: Int = defaultBufferSize) : super(reader, bufferLen)

    var readLineCnt: Long = 1
    private set


    constructor(string: String) : this(StringReader(string), string.length+1)


    val bufferedLines : List<String> get() {
        val position = markPositionInternal()
        val lines = getFromCurrentPositionToLastReadText().split("\n")
        reset(position)
        return lines
    }


    val currentLineCnt: Long
        get() {
            if(buffer.currentPositionFromLastRead == 0) return readLineCnt
            val position = markPositionInternal()
            var cnt = readLineCnt
            for (i in getFromCurrentPositionToLastRead()) {
                if(i == '\n'){
                    cnt --
                }
            }
            reset(position)
            return cnt
        }

    val positionInLine : Long
        get() {
            val currentPositionFromFirstReadBefore = currentPositionFromFirstRead
            val internalPosition = markPositionInternal()
            goBackTo('\n')
            val position = currentPositionFromFirstReadBefore - buffer.currentPositionFromFirstRead
            reset(internalPosition)
            return position
        }

    companion object{
        fun ofBuffered(reader: Reader, bufferLen: Int = defaultBufferSize): TextReaderWithMemory {
            return TextReaderWithMemory(BufferedReader(reader), bufferLen)
        }

        private fun Array<Int>.asText() : String {
            return this.toIntArray().asText()
        }

        private fun IntArray.asText() : String {
            return String(this, 0, this.size)
        }

        private fun CharArray.asText() : String {
            return String(this, 0, this.size)
        }
    }


    override fun read(cbuf: CharArray, off: Int, len: Int): Int {

        // we need to find a new lines only in a new read character
        //  when the reader achieves the end of buffer.currentPositionFromLastRead == 0
        // so we need to ignore buffered values, because they were already read and checked
        val bufferPosition = buffer.currentPositionFromLastRead
        val readLen =  super.read(cbuf, off, len)
        if(readLen != -1 && buffer.currentPositionFromLastRead == 0 && readLen > bufferPosition) {
            val fromIndex = off + bufferPosition
            val toIndex = fromIndex + readLen - 1
            for (i in fromIndex.. toIndex) {
                if (cbuf[i] == '\n') {
                    readLineCnt++
                }
            }
        }
        return readLen
    }



    fun readChar(): Char?{
        val next = read()
        if(next == -1) return null
        return next.toChar()
    }

    fun getFromCurrentPositionToLastReadText(): String{
       return getFromCurrentPositionToLastRead().asText()
    }

    fun readTextToNext(vararg char: Char): String?{
       return readToNext(*char).asText()
    }

}