package com.systema.kotlin.toolbox.reader

import com.systema.kotlin.toolbox.asText
import com.systema.kotlin.toolbox.readChar
import java.io.Reader


fun Reader.readChar(): Char? {
    val arr = CharArray(1)
    val code = this.read(arr)
    if(code == -1) return null
    return arr[0]
}

fun BiReader.getCurrentChar() = this.getCurrent().toChar()
fun BiReader.getNextChar() = this.getNext().toChar()
fun BiReader.getPrevChar() = this.getPrev().toChar()

fun BiReader.getFromCurrentPositionToLastReadAsText() = getFromCurrentPositionToLastRead().joinToString("")
fun BiReader.getFromFirstReadToCurrentAsText() = getFromFirstReadToCurrent().joinToString("")

private fun Array<Int>.asText(): String {
    val sb = StringBuilder()
    for (i in this) {
        sb.append(i.toChar())
    }
    return sb.toString()
}

private val BiReader.isEndOfBuffer : Boolean get() = currentPositionFromLastRead == 0L



fun BiReader.readToNext(text: String, inclusive: Boolean = true,  readLimit: Int  = 0, ): String{
    if(text.isEmpty()) return ""
    if(text.length == 1) {
        return readToNext(text[0], readLimit =  readLimit, inclusive = inclusive).asText()
    }

    val chars = text.chars().toArray()
    val charArray = mutableListOf<Char>()
    val sb = StringBuilder()
    var readCnt = 0
    for (i in chars.indices){
        if(isEndOfBuffer) readCnt++
        val char = readChar() ?: return ""
        charArray.add(char)
        sb.append(char)
        if(readLimit > 0 && readLimit == readCnt) return sb.toString()
    }
    var found = false
    while (!found){
        found = true

        for ((index, i) in chars.withIndex()) {
            if(charArray[index] != i.toChar()) {
                found = false
                break
            }
        }

        if(!found){
            if(!hasNext()) return sb.toString()
            charArray.removeFirst()
            if(isEndOfBuffer) readCnt++
            val char = readChar() ?: return sb.toString()
            if(isEndOfBuffer) readCnt++
            sb.append(char)
            charArray.add(char)
            if(readLimit > 0 && readLimit == readCnt) return sb.toString()
        }

    }

    if(!found || inclusive){
        return sb.toString()
    }

    goBack(text.length)
    return sb.dropLast(text.length).toString()
}


fun BiReader.goToNext(text: String, inclusive: Boolean = true, readLimit: Int  = 0): Boolean{
    if(text.isEmpty()) return true
    if(text.length == 1) {
        return goToNext(text[0], readLimit =  readLimit, inclusive = inclusive)
    }

    val textArray = text.toCharArray()

    var found = false

    val readBefore = currentPositionFromFirstRead
    var rest = readLimit
    val buff = CharArray(text.length)

    while (!found) {
        val hasFirstChar = goToNext(text[0], inclusive = false, readLimit = rest)
        if (!hasFirstChar) return false
        val readAfter = currentPositionFromFirstRead

        rest = (readAfter - readBefore).toInt()
        if(rest <= 0) return false

        for (char in textArray) {
            val arr = read(buff)

            if(arr == -1 || arr != text.length){
                return false
            }

            rest -= arr

            if(buff.contentEquals(textArray)){
                found = true
                break
            }
            if(rest <= 0) return false
        }

        if(!found){
            goBack(text.length-1)
        }
    }

    if(!inclusive) {
        goBack(text.length)
    }

    return true
}


fun BiReader.readTextNext(cnt: Int) : String{
    return readNext(cnt).asText()
}


fun BiReader.readTextNextAndMove(cnt: Int) : String{
    return readNextAndMove(cnt).asText()
}


fun BiReader.readNext(cnt: Int) : Array<Int>{
    val array = readNextAndMove(cnt)
    goBack(array.size)
    return array
}


fun BiReader.readNextAndMove(cnt: Int) : Array<Int>{
    val array = Array(cnt){0}
    for (i in 0 until cnt) {
        if(!this.goNext())  return array.copyOfRange(0, i)
        array[i] = this.getCurrent()
    }
    return array
}

fun BiReader.readToNextTrimmed(vararg char: Char): String? {
    return readToNextOrNull(*char)?.asText()?.trim()
}

fun BiReader.getCurrentOrReadAndReturnBack(): Int {
    return if(!hasCurrent()) {
        val r = read()
        goBack()
        r
    }
    else getCurrent()
}

fun BiReader.goLeftFrom(vararg char: Char): Boolean {
    if(goBackTo(*char, inclusive = true)){
        goBack()
        return true
    }
    return false
}

/**
 * @param trimEnd -> trim end of read line
 * @param jumpToNextLine - if true -> move reader to beginn of next line, else stop before \n
 */
fun BiReader.readToLineBreak(trimEnd: Boolean = true, jumpToNextLine: Boolean = true): String {
    if(!hasNext()) return ""
    if(getCurrentChar() == '\n') {
        if(jumpToNextLine){
            read() // read "\n" -> after new line
        }
        return ""
    };
    val text = readToNextIncludingCurrent( '\n').asText()
    if(getNextChar() == '\n' && jumpToNextLine){
        goNext() //  read current -> "\n"
        read() // read "\n" -> after new line
    }
    else{
        goNext() //  read current -> "\n"
    }
    if(trimEnd) return text.trimEnd()
    return text
}

fun BiReader.goBackToLineBegin(inclusive: Boolean = true) {

    if(getPrev() == '\n'.code) {
        if(inclusive) goBack()
        return
    }
    if(getCurrent() == '\n'.code && inclusive){
        return
    }
    if(!goBackTo('\n', inclusive = inclusive)){
        goToFirstReadBuffered()
    }
}

fun BiReader.goToLineBreak(startFromNewLine: Boolean = true) {
    if(getCurrent() == '\n'.code) return
    goToNext('\n')
    if(startFromNewLine) goNext()
}


fun BiReader.readToLineBreakTrimmed() = readToNext( '\n', inclusive = true).asText().trim()

private const val spaceChar = ' '
private const val spaceCode = ' '.code
private const val tabCode = '\t'.code

fun BiReader.skipSpaces(): BiReader {
    val reader = this
    var next : Char = spaceChar
    val array = CharArray(1)
    while (next == spaceChar) {
        val res = reader.read(array)
        if(res == -1) {
            return reader
        }
        next = array[0]
    }
    if (reader.getPrev() != spaceCode && reader.getPrev() != tabCode) {
        reader.goBack()
    }
    return reader
}

