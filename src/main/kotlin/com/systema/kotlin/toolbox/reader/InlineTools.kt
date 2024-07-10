package com.systema.kotlin.toolbox.reader

import java.io.Reader


fun Reader.readChar() = read().toChar()
fun BiReader.getCurrentChar() = this.getCurrent().toChar()
fun BiReader.getNextChar() = this.getNext().toChar()
fun BiReader.getPrevChar() = this.getPrev().toChar()

fun BiReader.getFromCurrentPositionToLastReadAsText() = getFromCurrentPositionToLastRead().joinToString("")
fun BiReader.getFromFirstReadToCurrentAsText() = getFromFirstReadToCurrent().joinToString("")
