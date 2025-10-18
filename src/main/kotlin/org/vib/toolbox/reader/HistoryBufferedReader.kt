package org.vib.toolbox.reader


interface HistoryBufferedReader {

    /**
     * Defines where the cursor should end up relative to the match.
     */
    enum class MatchPosition {
        BEFORE, AFTER
    }

    fun goForward(steps: Int): Boolean
    fun goForward(): Boolean
    fun goBack(): Boolean
    fun goBack(steps: Int): Boolean
    fun goBackAndGet(): Char?
    fun goBackAndGet(steps: Int): Char?

    fun hasCurrent() : Boolean
    fun hasPrevious(): Boolean
    fun hasNext() : Boolean

    fun peekCurrent() : Char?
    fun peekNext(): Char?
    fun peekPrevious() : Char?

    fun goBackTo(
        vararg targets: String,
        matchPosition: MatchPosition = MatchPosition.BEFORE,
        resetOnFail: Boolean = false,
        limit: Int = 0
    ): Boolean

    fun goBackTo(
        vararg targets: Char,
        matchPosition: MatchPosition = MatchPosition.BEFORE,
        resetOnFail: Boolean = false,
        limit: Int = 0
    ): Boolean



    data class ReadLimit(val size: Int){
        companion object{
            val UNLIMITED = ReadLimit(0)
            val END_OF_BUFFER = ReadLimit(-1)
            fun LIMITED(size: Int) = ReadLimit(size)
        }
    }



    fun goForwardTo(vararg targets: String,
                    matchPosition: MatchPosition = MatchPosition.BEFORE,
                    resetOnFail: Boolean = false,
                    readLimit: Int): Boolean


    fun goForwardTo(vararg targets: String,
                    matchPosition: MatchPosition = MatchPosition.BEFORE,
                    resetOnFail: Boolean = false,
                    readLimit: ReadLimit = ReadLimit.UNLIMITED): Boolean


    fun goForwardTo(vararg targets: Char,
                    matchPosition: MatchPosition = MatchPosition.BEFORE,
                    resetOnFail: Boolean = false,
                    readLimit: Int): Boolean


    fun goForwardTo(vararg targets: Char,
                    matchPosition: MatchPosition = MatchPosition.BEFORE,
                    resetOnFail: Boolean = false,
                    readLimit: ReadLimit = ReadLimit.UNLIMITED): Boolean


    fun readTo(vararg targets: Char,
                    matchPosition: MatchPosition = MatchPosition.BEFORE,
                    resetOnFail: Boolean = false,
                    nullIfNotFound: Boolean = false,
                    readLimit: Int = 0): String?

    fun readTo(vararg targets: String,
               matchPosition: MatchPosition = MatchPosition.BEFORE,
               resetOnFail: Boolean = false,
               nullIfNotFound: Boolean = false,
               readLimit: Int = 0): String?


    fun getFromFirstReadToCurrent(): String
    fun readFromCurrentToEnd(): String

    fun markPosition(): Long
    fun resetPosition(markedPosition: Long)

}