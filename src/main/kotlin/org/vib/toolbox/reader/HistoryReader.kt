package org.vib.toolbox.reader

import java.io.Reader

abstract class HistoryReader : Reader(), IHistoryReader{
    companion object{
        @JvmStatic
        fun ofBuffered(reader: Reader): HistoryBufferedReader{
            return HistoryBufferedReader(reader)
        }
    }

    protected abstract fun calculateEndBufferReadLimit(readLimit: IHistoryReader.ReadLimit): Int?

    override fun goBackAndGet(): Char? {
        if (!goBack()) return null
        if (!hasCurrent()) return null
        return peekCurrent()
    }

    override fun goBack(steps: Int): Boolean {
        for (i in 0 until steps) {
            if (!goBack()) return false
        }
        return true
    }

    override fun peekPrevious(): Char? {
        if (!hasPrevious()) return null
        goBack()
        val c = peekCurrent()
        goForward()
        return c
    }

    override fun goForward(steps: Int): Boolean {
        for (i in 0 until steps) {
            if (!goForward()) return false
        }
        return true
    }



    override fun goBackAndGet(steps: Int): Char? {
        if (!goBack(steps - 1)) return null
        return goBackAndGet()
    }

    override fun peekNext(): Char? {
        if (!goForward()) return null
        val c = peekCurrent()
        goBack()
        return c
    }






    override fun goForwardTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: IHistoryReader.ReadLimit
    ): Boolean {
        val limit = calculateEndBufferReadLimit(readLimit) ?: return false
        return goForwardTo(targets = targets, matchPosition, resetOnFail, limit)
    }



    override fun readTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        nullIfNotFound: Boolean,
        readLimit: IHistoryReader.ReadLimit
    ): String? {
        val limit = calculateEndBufferReadLimit(readLimit) ?: return null
        return readTo(targets = targets, matchPosition, resetOnFail, nullIfNotFound, limit)
    }

    override fun readTo(
        vararg targets: Char,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        nullIfNotFound: Boolean,
        readLimit: IHistoryReader.ReadLimit
    ): String? {
        val limit = calculateEndBufferReadLimit(readLimit) ?: return null
        return readTo(targets = targets, matchPosition, resetOnFail, nullIfNotFound, limit)
    }


    override fun readTo(
        vararg targets: StrPattern,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        nullIfNotFound: Boolean,
        readLimit: IHistoryReader.ReadLimit
    ): String? {
        val limit = calculateEndBufferReadLimit(readLimit) ?: return null
        return readTo(targets = targets, matchPosition, resetOnFail, nullIfNotFound, limit)
    }


    override fun goForwardTo(
        vararg targets: Char,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: IHistoryReader.ReadLimit
    ): Boolean {
        val limit = calculateEndBufferReadLimit(readLimit) ?: return false
        return goForwardTo(targets = targets, matchPosition, resetOnFail, limit)
    }
}