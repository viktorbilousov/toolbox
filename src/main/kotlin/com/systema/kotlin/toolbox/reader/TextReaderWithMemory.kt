package com.systema.kotlin.toolbox.reader

import java.io.BufferedReader
import java.io.Reader
import java.io.StringReader

open class TextReaderWithMemory: ReaderWithMemory {
    constructor(reader: Reader, bufferLen: Int = defaultBufferSize) : super(reader, bufferLen)

    var readLineCnt: Long = 0
    private set


    constructor(string: String) : this(StringReader(string), string.length+1)


    val bufferedLines : List<String> get() {
        buffer.markPosition()
        buffer.goToFirstRead()
        return getFromCurrentPositionToLastReadText().split("\n")
    }


    val currentLineCnt: Long
        get() {
            if(buffer.currentPositionFromLastRead == 0) return readLineCnt
            buffer.markPosition()
            var cnt = readLineCnt;
            for (i in getFromCurrentPositionToLastRead()) {
                if(i == '\n'){
                    cnt --;
                }
            }
            buffer.moveToMarkedPosition()
            return cnt
        }

    val positionInLine : Long
        get() {
            val currentPositionFromFirstReadBefore = currentPositionFromFirstRead
            buffer.markPosition()
            goBackTo('\n')
            val position = currentPositionFromFirstReadBefore - buffer.currentPositionFromFirstRead
            buffer.moveToMarkedPosition()
            return position.toLong()
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

        val bufferPosition = buffer.currentPositionFromLastRead;
        val readLen =  super.read(cbuf, off, len)
        if(readLen != -1) return readLen;
        if(bufferPosition == 0) return readLen
        for(i in off + bufferPosition .. len){
            if(cbuf[i] == '\n'){
                readLineCnt++;
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
       return readToNext(*char)?.asText()
    }

}