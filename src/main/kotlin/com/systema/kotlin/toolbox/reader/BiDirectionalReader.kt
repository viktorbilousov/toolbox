package com.systema.kotlin.toolbox.reader

interface BiDirectionalReader {

    val currentPositionFromLastRead: Long
    val currentPositionFromFirstRead: Long


    fun hasNext(): Boolean
    fun hasCurrent(): Boolean
    fun hasPrevious(): Boolean

    fun goNext(): Boolean
    fun goBack(): Boolean

    fun goBack(steps: Int): Boolean {
        for (i in 1..steps) {
            if (!goBack()) return false
        }
        return true
    }

    fun getPrev(steps: Int): Int

    fun getCurrent(): Int

    fun getPrev(): Int {
        if (!hasPrevious()) return -1
        val res = getPrevAndMove()
        goNext()
        return res
    }

    fun getNext(): Int {
        if (!hasNext()) return -1
        goNext()
        val res = getCurrent()
        goBack()
        return res
    }

    fun getPrevAndMove(): Int {
        if (!hasPrevious()) return -1
        goBack()
        return getCurrent()
    }

    fun getFirstReadAndMove(): Int {
        if (!goToFirstRead()) return -1
        return getCurrent()
    }

    fun getLastReadAndMove(): Int {
        if (!goToLastRead()) return -1
        return getCurrent()
    }

    fun getPrevAndMove(steps: Int): Int

    fun goToFirstRead(): Boolean
    fun goToLastRead(): Boolean


    fun getLastRead(): Int
    fun getFirstRead(): Int


    fun getFromCurrentPositionToLastRead(): CharArray

    fun goBackTo(vararg char: Char, include: Boolean = false): Boolean
    fun goToNext(vararg char: Char, include: Boolean = false): Boolean


    fun goBackToCharAndGetAllToLastRead(vararg char: Char, include: Boolean = false): CharArray
    fun goBackToCharAndGetAllToLastReadOrNull(vararg char: Char, include: Boolean = false): CharArray?
    fun getFromFirstReadToCurrent(): CharArray


    fun readToNext(vararg char: Char, include: Boolean = false): CharArray
    fun readToNextOrNull(vararg char: Char, include: Boolean = false): CharArray?
    fun readToNextIncludingCurrent(vararg char: Char, include: Boolean = false): CharArray
    fun readToNextIncludingCurrentOrNull(vararg char: Char, include: Boolean = false): CharArray?
}