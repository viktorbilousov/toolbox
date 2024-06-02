package com.systema.kotlin.toolbox

import java.io.StringReader
import java.util.LinkedList
import kotlin.math.max

@Deprecated("Use TextReaderWithMemory(StreamReader(str))")
open class StringStreamReader(str: String, pufferLen : Int = 300): StringReader(str) {

    private val sb = java.lang.StringBuilder()
    var returnLastByManualReadNext  = false;
    val pufferLen: Int = max(1, pufferLen)
    val puffer = ArrayList<Char>(pufferLen+1)
    private var pointer : Int = 0
        private set

    var currentLineCnt : Long = 1
        private set

    var readCharactersInLineCnt : Long = 0
        private set

    var readCharactersCnt : Long = 0
    private set

    val currentPositionIndex get() = readCharactersInLineCnt - pointer - 1

    private fun putToPuffer(char: Char){
        puffer.add(0, char)  //BAD
        if(puffer.size > pufferLen) puffer.removeAt(pufferLen)
    }

//    private fun popFromPuffer() : Char? {
//        if(puffer.size == 0) return null
//        val el = puffer[0]
//        puffer.removeAt(0)
//        return el;
//    }

    val pufferedLines : List<String> get() = puffer.reversed().joinToString("").split("\n")

    fun getPrev() : Char? {
        if(!goBack()) return null
        return readNext()
    }

    fun goBackToIncluded(vararg char: Char): Boolean{
        if(!goBackTo(*char)) return false
        return goBack()
    }

    fun goBackTo(vararg char: Char): Boolean{
        while (true){
            val prev = getPrev() ?: return false
            if(char.contains(prev)) break
            goBack()
        }
        return true
    }

    fun goBackToLineBegin() :Boolean{
        return goBackTo('\n')
    }
    fun goBackFromLineEndToLineBegin() :Boolean{
        return  goBack(1) &&   goBackTo('\n')
    }

    fun goBack(steps: Int) : Boolean {
        return goBack(steps.toLong())
    }

    fun goBack(steps: Long = 1) : Boolean {
        for (i in 1..steps) {
            if (pointer + 1 > pufferLen) return false
            pointer++
        }
        return true
    }


    fun getLastReadCharacter() = puffer.getOrNull(0)

    fun getLastReadOrPointedCharacter() : Char? {
        return if(pointer > 0) puffer[pointer]
        else getLastReadCharacter()
    }

    fun readNext() : Char? {
        if(pointer > 0) return puffer[--pointer]
        val charByte = this.read()
        if(charByte == -1) return null
        val char = charByte.toChar()
        putToPuffer(char)
        if(char == '\n') {
            currentLineCnt ++
            readCharactersInLineCnt = 0
        }
        else {
            readCharactersInLineCnt++
        }
        readCharactersCnt++
        return char
    }

    fun readToNext(str: String): String{
        sb.clear()
        val lastChar = str.last()

        while (true) {
            val text = readToNextIncluded(lastChar)
            if(text.isEmpty()) return text
            if(text.endsWith(str)) return text.substringBeforeLast(str)
        }
    }

    fun readToNextIncluded(str: String): String{
        sb.clear()
        val lastChar = str.last()

        while (true) {
            val text = readToNextIncluded(lastChar)
            if(text.isEmpty()) return text
            if(text.endsWith(str)) return text
        }
    }

    fun readToNext(vararg char: Char): String{
        sb.clear()
        var readChar : Char?
        while (true) {
            readChar = readNext() ?: break
            if(char.contains(readChar)) break
            sb.append(readChar)
        }
        val str = sb.toString()
        sb.clear()
        return str
    }

    fun readToNextIncluded(vararg char: Char): String{
        val str = readToNext(*char)
        val prev = getLastReadCharacter()
        return str + prev
    }

    fun readToNextTrimmed(vararg char: Char): String {
        return readToNext(*char).trim()
    }

    fun readToNextSpace() = readToNext(' ')
    fun readToLineBreak(trimEnd: Boolean = true) = readToNext( '\n').let { if(trimEnd) it.trimEnd() else it }
    fun readToLineBreakTrimmed() = readToNext( '\n').trim()

    fun skipSpaces() : StringStreamReader{
        var next : Char? = ' '
        while (next == ' ')  next = readNext()
        if(next != null) this.goBack()
        return this
    }

    fun hasNext(): Boolean {
        val next = readNext()
        goBack()
        return next != null
    }

}