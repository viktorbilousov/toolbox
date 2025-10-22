package org.vib.toolbox.reader
import org.vib.toolbox.reader.HistoryBufferedReader.MatchPosition
import java.io.IOException
import java.io.Reader
import java.util.*
import kotlin.math.abs

open class DoubleBufferedReader(
    private val input: Reader,
    private val bufferSize: Int = 8192*2
) : Reader(), HistoryBufferedReader {
    private val half = bufferSize / 2
    private val bufferA = CharArray(half)
    private val bufferB = CharArray(half)

    private var headBuffer = bufferA
    private var historyBuffer = bufferB

    private var pos = 0
    private var limit = 0
    private var historyRelativePosition = 0;
    private var endReached = false
    private var isHistoryBufferEmpty = true
    private var bufferCounter = 0;
    private var isOpen = true
    private var lock = Any()

    companion object {
        val defaultCharBufferSize = 8192
        val defaultExpectedLineLength = 80

        /**
         * For iteration in kotlin is slow
         */
        @JvmStatic
        private fun compareChar(c: Char, a: CharArray): Boolean {
            val size = a.size

            if (size >= 1) {
                if (c == a[0]) return true
                if (size == 1) return false
            }
            if (size >= 2) {
                if (c == a[1]) return true
                if (size == 2) return false
            }
            if (size >= 3) {
                if (c == a[2]) return true
                if (size == 3) return false
            }
            if (size >= 4) {
                if (c == a[3]) return true
                if (size == 4) return false
            }
            if (size >= 5) {
                if (c == a[4]) return true
                if (size == 5) return false
            }
            if (size >= 6) {
                if (c == a[5]) return true
                if (size == 6) return false
            }
            if (size >= 7) {
                if (c == a[6]) return true
                if (size == 7) return false
            }
            if (size >= 8) {
                if (c == a[7]) return true
                if (size == 8) return false
            }
            if (size >= 9) {
                if (c == a[8]) return true
                if (size == 9) return false
            }
            if (size >= 10) {
                if (c == a[9]) return true
                if (size == 10) return false
            }
//
            if (size > 10) {
                var i = 10;
                while (i < size) {
                    if (c == a[i]) return true
                    i++
                }
            }
            return false
        }
    }

    override fun getFromFirstReadToCurrent(): String {
        if (limit == 0) return "";
        val absoluteposition = pos - historyRelativePosition
        if (absoluteposition < 0) {
            if (isHistoryBufferEmpty) return ""
            return String(historyBuffer, 0, absoluteposition + half + 1)
        } else {
            if (isHistoryBufferEmpty) {
                return String(headBuffer, 0, absoluteposition)
            } else {
                val sb = StringBuilder(half * 2)
                sb.append(historyBuffer)
                sb.append(headBuffer, 0, absoluteposition)
                return sb.toString()
            }
        }
    }


    override fun readFromCurrentToEnd(): String {
        if (historyRelativePosition == 0) return ""
        val absoluteposition = pos - historyRelativePosition
        if (absoluteposition < 0) {
            if (isHistoryBufferEmpty) return String(historyBuffer, 0, pos + 1)
            val sb = StringBuilder(half * 2)
            sb.append(historyBuffer, absoluteposition + half, abs(absoluteposition))
            sb.append(headBuffer, 0, pos)
            return sb.toString()
        } else {
            return String(headBuffer, absoluteposition, pos - absoluteposition)
        }
    }

    override fun read(): Int {
        ensureOpen()
        synchronized(lock) {
            if (historyRelativePosition > 0) {
                if (pos - historyRelativePosition < 0) {
                    if (isHistoryBufferEmpty) return -1;
                    val index = pos - historyRelativePosition + half
                    historyRelativePosition--
                    return historyBuffer[index].code
                } else {
                    val index = pos - historyRelativePosition
                    historyRelativePosition--
                    return headBuffer[index].code
                }
            } else {
                if (pos >= limit) {
                    if (!fillNextBuffer()) return -1
                }
                return headBuffer[pos++].code
            }
        }
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        ensureOpen()
        synchronized(lock) {
            var totalRead = 0

            while (totalRead < len) {
                val absolutePosition = pos - historyRelativePosition
                // read from stream
                if (absolutePosition >= limit) {
                    if (!fillNextBuffer()) break
                }
                var currentPosition = pos - historyRelativePosition

                // read from history buffer
                if (pos - historyRelativePosition < 0) {
                    val positionInHistoryBuffer = pos - historyRelativePosition + half
                    val available = minOf(half - positionInHistoryBuffer, len - totalRead)
                    historyBuffer.copyInto(
                        cbuf,
                        off + totalRead,
                        positionInHistoryBuffer,
                        positionInHistoryBuffer + available
                    )
                    historyRelativePosition -= available
                    totalRead += available
                    continue;
                }

                val available = minOf(limit - currentPosition, len - totalRead)
                headBuffer.copyInto(cbuf, off + totalRead, currentPosition, currentPosition + available)
                historyRelativePosition -= available
                if (historyRelativePosition < 0) {
                    pos -= historyRelativePosition // pos = pos - (-history) = pos + history
                    historyRelativePosition = 0
                }
                totalRead += available
            }
            return if (totalRead == 0 && endReached) -1 else totalRead
        }
    }

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

    override fun goForward(): Boolean {
        if (historyRelativePosition != 0) {
            historyRelativePosition--;
            return true
        } else {
            return read() != -1
        }
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

    override fun hasNext(): Boolean {
        if (endReached) return false

        val absolutePosition = pos - historyRelativePosition
        // start of file
        if (limit == 0) {
            val res = goForward()
            pos = 0 // reset to fist position
            return res
        } else if (absolutePosition == limit) {
            val res = goForward()
            pos--;
            return res
        }
        return true
    }

    override fun hasPrevious(): Boolean {
        if (limit == 0) return false
        // pos is pointer to "next to read". If pos = 1 -> first was read -> ok
        val absolutePosition = pos - historyRelativePosition - 1
        if (absolutePosition == 0 && isHistoryBufferEmpty) {
            return false
        }
        if (absolutePosition + half < 0) return false
        return true
    }

    private fun canBeMovedBack(): Boolean {
        if (limit == 0) return false
        // pos is pointer to "next to read". If pos = 1 -> first was read -> ok
        val absolutePosition = pos - historyRelativePosition
        if (absolutePosition == 0 && isHistoryBufferEmpty) {
            return false
        }
        if (absolutePosition + half < 0) return false
        return true
    }


    override fun goBack(): Boolean {
        // no read values
        if (!canBeMovedBack()) {
            return false;
        }

        // Move backward
        historyRelativePosition++;

        val absolutePosition = pos - historyRelativePosition

        if (absolutePosition < 0) {
            // check in history buffer
            if (!isHistoryBufferEmpty && absolutePosition + half >= 0) {
                return true
            } else {
                // go forward to first read character
                historyRelativePosition--
                return false;
            }
        } else {
            return true
        }
    }

    override fun peekCurrent(): Char? {
        // pos is pointer to "next to read", so current will be pos-1
        var position = pos - 1 - historyRelativePosition
        if (position < 0) {
            if (isHistoryBufferEmpty) return null
            position += half
            return historyBuffer[position]
        }
        return headBuffer[position]


    }


    override fun hasCurrent(): Boolean {
        ensureOpen()
        if (limit == 0) return false
        // pos is pointer to "next to read", so current will be pos-1
        val absolutePosition = pos - historyRelativePosition
        if (absolutePosition - 1 < 0 && isHistoryBufferEmpty) return false
        if (absolutePosition - 1 + half < 0) return false
        return true
    }


    override fun goBackTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        limit: Int
    ): Boolean {
        val targets = targets.filter { it.isNotEmpty() }
        if (targets.isEmpty()) return false
        if (targets.all { it.length == 1 }) {
            val arr = targets.map { it[0] }.toCharArray()
            return goBackTo(targets = arr, matchPosition = matchPosition, resetOnFail = resetOnFail, limit = limit)
        }

        var startPos = historyRelativePosition
        val maxTargetLen = targets.maxOf { it.length }

        // Circular buffer to hold last scanned characters
        val window = CharArray(maxTargetLen)
        var windowSize = 0

        var cnt = 0
        while (limit == 0 || cnt < limit) {
            val ch = goBackAndGet() ?: break
            cnt++;
            // Add character to the circular window
            if (windowSize < maxTargetLen) {
                window[windowSize++] = ch
            } else {
                // Shift left to make room for new char
                for (i in 0 until maxTargetLen - 1) window[i] = window[i + 1]
                window[maxTargetLen - 1] = ch
            }

            // Check each target
            for (target in targets) {
                if (windowSize >= target.length) {
                    var matched = true
                    for (i in target.indices) {
                        if (window[windowSize - 1 - i] != target[i]) {
                            matched = false
                            break
                        }
                    }
                    if (matched) {
                        when (matchPosition) {
                            MatchPosition.BEFORE -> { /* already before match */ goBack()
                            }

                            MatchPosition.AFTER -> repeat(target.length - 1) { read() }
                        }
                        return true
                    }
                }
            }
        }

        if (resetOnFail) {
            historyRelativePosition = startPos
        }
        return false
    }

    private fun fillNextBuffer(): Boolean {
        if (endReached) return false

        val readCount = input.read(historyBuffer, 0, half)
        if (readCount <= 0) {
            endReached = true
            return false
        }
        if (limit != 0) {
            isHistoryBufferEmpty = false
        }

//        inactiveLimit = limit
        val tmp = headBuffer
        headBuffer = historyBuffer
        historyBuffer = tmp
        pos = 0
        historyRelativePosition = 0
        limit = readCount
        bufferCounter++;
        return true
    }

    private fun calculateEndBufferReadLimit(readLimit: HistoryBufferedReader.ReadLimit): Int? {
        if (readLimit == HistoryBufferedReader.ReadLimit.END_OF_BUFFER) {
            if (limit == 0) {
                if (!hasNext()) return null
            }
            val absolutePosition = pos - historyRelativePosition

            // end of the buffer
            if (absolutePosition == limit) {
                return half
            }

            if (isHistoryBufferEmpty) {
                return half * 2 - absolutePosition
            } else {
                return half - absolutePosition
            }
        } else {
            return readLimit.size
        }
    }

    override fun goForwardTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: HistoryBufferedReader.ReadLimit
    ): Boolean {
        val limit = calculateEndBufferReadLimit(readLimit) ?: return false
        return goForwardTo(targets = targets, matchPosition, resetOnFail, limit)
    }

//    override fun goForwardTo(
//        vararg targets: String,
//        matchPosition: MatchPosition,
//        resetOnFail: Boolean,
//        readLimit: Int
//    ): Boolean {
//        val targets = targets.filter { it.isNotEmpty() }
//        if (targets.isEmpty()) return false
//        if(targets.all { it.length == 1 }){
//            val arr = targets.map { it[0] }.toCharArray()
//            return goForwardTo(targets = arr, matchPosition= matchPosition, resetOnFail = resetOnFail, readLimit = readLimit)
//        }
//
//        val position = markPosition()
//        val maxTargetLen = targets.maxOf { it.length }
//
//        val window = CharArray(maxTargetLen)
//        var windowSize = 0
//        var count = 0
//
//        while (readLimit == 0 || count++ < readLimit) {
//            val ch = read()
//            if(ch == -1) break;
//
//            // Add character to sliding window
//            if (windowSize < maxTargetLen) {
//                window[windowSize++] = ch.toChar()
//            } else {
//                // Shift left
//                for (i in 0 until maxTargetLen - 1) window[i] = window[i + 1]
//                window[maxTargetLen - 1] = ch.toChar()
//            }
//
//            // Check for match
//            for (target in targets) {
//                if (windowSize >= target.length) {
//                    var matched = true
//                    for (i in target.indices) {
//                        // Compare most recent chars
//                        if (window[windowSize - target.length + i] != target[i]) {
//                            matched = false
//                            break
//                        }
//                    }
//
//                    if (matched) {
//                        when (matchPosition) {
//                            MatchPosition.BEFORE -> {
//                                // Step back to just before match
//                                repeat(target.length) { goBack() }
//                            }
//                            MatchPosition.AFTER -> {
//                                // Already after match → do nothing
//                            }
//                        }
//                        return true
//                    }
//                }
//            }
//        }
//
//        if (resetOnFail) {
//           resetPosition(position)
//        }
//        return false
//    }


    override fun goForwardTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: Int
    ): Boolean {

        if (targets.isEmpty()) return false

        val size = targets.size
        val pattern = arrayOfNulls<StrPattern>(size)
        for (i in 0 until size) {
            pattern[i] = StrPattern(targets[i])
        }
        return goForwardTo(targets = (pattern as Array<StrPattern>), matchPosition, resetOnFail, readLimit)

    }


    fun goForwardTo(
        vararg targets: StrPattern,
        matchPosition: MatchPosition = MatchPosition.BEFORE,
        resetOnFail: Boolean = false,
        readLimit: Int = 0
    ): Boolean {

        if (targets.isEmpty()) return false

        val startMark = markPosition()
        val size = targets.size
        val firstChars = CharArray(size)
        for (i in 0 until size) {
            firstChars[i] = targets[i].string[0]
        }
        var restReadLimit = readLimit

        var read = -1
        var ch = Char(0)

        val pointers = IntArray(size)
        val sizes = IntArray(size) { targets[it].string.length }
        var finished = true
        var found = BooleanArray(size)
        var foundAny = false
        var index = -1;
        var foundIndex = -1;
        var maxLen = 0;
        val unlimited = readLimit == 0

        while (unlimited || restReadLimit > 0) {

            val findFirst =
                goForwardTo(targets = firstChars, matchPosition = MatchPosition.BEFORE, resetOnFail, restReadLimit)

            if (!findFirst) {
                return false
            }

            index = -1
            finished = true
            Arrays.fill(pointers, 0)

            if (!unlimited) {
                restReadLimit = readLimit - (markPosition() - startMark).toInt()
            }
            // execute KNC
            while (unlimited || restReadLimit > 0) {

                finished = true
                read = read()
                if (read == -1) {
                    return false // EOF
                }
                index++
                if (!unlimited) restReadLimit--

                ch = read.toChar()

                for (i in 0 until size) {
                    if (found[i]) continue
                    while (true) {
                        if (targets[i].string[pointers[i]] == ch) {
                            pointers[i]++
                            // found!
                            if (pointers[i] == sizes[i]) {
                                foundAny = true
                                found[i] = true
                                foundIndex = index
                                if (pointers[i] > maxLen) {
                                    maxLen = pointers[i]
                                }
                                break
                            }
                            finished = false
                            break
                        } else {
                            if (pointers[i] <= 0) {
                                break
                            } else {
                                finished = false
                                pointers[i] = targets[i].lps[pointers[i] - 1]
                                if (pointers[i] == 0) break
                            }
                        }
                    }
                }

                if (finished) {
                    break
                }
            }

            if (foundAny) {

                when (matchPosition) {
                    MatchPosition.BEFORE -> goBack(maxLen)
                    MatchPosition.AFTER -> {}
                }

                return true
            } else {
                restReadLimit -= index
            }

        }

        if (resetOnFail) {
            resetPosition(startMark)
        }
        return false

    }


    override fun goForwardTo(
        vararg targets: Char,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: Int
    ): Boolean {
        if (targets.isEmpty()) return false

        val positionBefore = markPosition()
        var startedPosition: Int
        var found: Boolean
        var c: Char
        var foundPosition: Int
        var endedInHead = false
        var limitReached = false
        var readCnt = 0;
        ensureOpen()
        synchronized(lock) {
            bufferLoop@ while (true) {
                var aPos = pos - historyRelativePosition
                if (aPos >= limit) {
                    fillNextBuffer()
                    aPos = pos - historyRelativePosition
                }
                if (aPos >= limit || limitReached) { /* EOF */

                    if (resetOnFail) {
                        resetPosition(positionBefore)
                    }

                    return false
                }
                found = false
                c = Char(0)

                foundPosition = aPos
                startedPosition = aPos
                endedInHead = false
                charLoop@ while (foundPosition < limit) {
                    if (foundPosition < 0) {
                        c = historyBuffer[foundPosition + half]
                        endedInHead = false
                    } else {
                        c = headBuffer[foundPosition]
                        endedInHead = true
                    }


                    if (compareChar(c, targets)) {
                        found = true
                        break@charLoop
                    }


                    foundPosition++
                    readCnt++

                    if (readLimit > 0 && readCnt >= readLimit) {
                        limitReached = true
                        break@charLoop
                    }
                }


                if (found) {
                    when (matchPosition) {
                        MatchPosition.BEFORE -> {}
                        MatchPosition.AFTER -> {
                            foundPosition++
                        }
                    }
                }

                if (foundPosition < pos) {
                    historyRelativePosition = pos - foundPosition
                } else {
                    pos = foundPosition
                    historyRelativePosition = 0
                }

                if (found) {
                    return true
                }
            }
        }

    }


    override fun readTo(
        vararg targets: Char,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        nullIfNotFound: Boolean,
        readLimit: Int
    ): String? {
        if (targets.isEmpty()) return null

        val positionBefore = markPosition()
        var startedPosition: Int
        var found: Boolean
        var c: Char
        var foundPosition: Int
        var endedInHead = false
        var limitReached = false
        var prevRead = 0;
        ensureOpen()
        synchronized(lock) {
            var stringBuffer: StringBuffer? = null
            bufferLoop@ while (true) {
                var aPos = pos - historyRelativePosition
                if (aPos >= limit) {
                    fillNextBuffer()
                    aPos = pos - historyRelativePosition
                }
                if (aPos >= limit || limitReached) { /* EOF */

                    if (resetOnFail) {
                        resetPosition(positionBefore)
                    }

                    if (!nullIfNotFound) {
                        return stringBuffer?.toString() ?: ""
                    } else {
                        return null
                    }
                }
                found = false
                c = Char(0)

                val startedInHistory = aPos < 0
                foundPosition = aPos
                startedPosition = aPos
                endedInHead = false
                prevRead = stringBuffer?.length ?: 0
                charLoop@ while (foundPosition < limit) {
                    if (foundPosition < 0) {
                        c = historyBuffer[foundPosition + half]
                        endedInHead = false
                    } else {
                        c = headBuffer[foundPosition]
                        endedInHead = true
                    }


                    if (compareChar(c, targets)) {
                        found = true
                        break@charLoop
                    }


                    foundPosition++

                    if (readLimit > 0 && prevRead + (foundPosition - aPos) >= readLimit) {
                        limitReached = true
                        break@charLoop
                    }
                }


                if (found) {
                    when (matchPosition) {
                        MatchPosition.BEFORE -> {}
                        MatchPosition.AFTER -> {
                            foundPosition++
                        }
                    }
                }

                if (foundPosition < pos) {
                    historyRelativePosition = pos - foundPosition
                } else {
                    pos = foundPosition
                    historyRelativePosition = 0
                }

                if (found) {

                    val str: String
                    if (stringBuffer == null) {
                        if (startedInHistory) {
                            if (!endedInHead) {
                                str = String(historyBuffer, startedPosition + half, foundPosition - startedPosition)
                            } else {
                                stringBuffer = StringBuffer(defaultExpectedLineLength)
                                stringBuffer.append(
                                    historyBuffer,
                                    startedPosition + half,
                                    historyBuffer.size - startedPosition
                                )
                                stringBuffer.append(headBuffer, 0, foundPosition)
                                str = stringBuffer.toString()
                            }
                        } else {
                            str = String(headBuffer, startedPosition, foundPosition - startedPosition)
                        }
                    } else {
                        if (startedInHistory) {
                            if (!endedInHead) {
                                stringBuffer.append(
                                    historyBuffer,
                                    startedPosition + half,
                                    foundPosition - startedPosition
                                )
                            } else {
                                stringBuffer.append(historyBuffer, startedPosition + half, abs(aPos))
                                stringBuffer.append(headBuffer, 0, foundPosition)
                            }
                        } else {
                            stringBuffer.append(headBuffer, startedPosition, foundPosition - startedPosition)
                        }
                        str = stringBuffer.toString()
                    }

                    return str
                }

                if (stringBuffer == null) {
                    stringBuffer = StringBuffer(defaultExpectedLineLength)
                }
                if (startedInHistory) {
                    stringBuffer.append(historyBuffer, startedPosition + half, abs(startedPosition))
                    stringBuffer.append(headBuffer)
                } else {
                    stringBuffer.append(headBuffer, startedPosition, foundPosition - startedPosition)
                }
            }
        }

    }


    private fun readToBuffer(
        vararg targets: Char,
        stringBuffer: StringBuilder,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: Int
    ): Boolean {
        if (targets.isEmpty()) return false

        val positionBefore = markPosition()
        var startedPosition: Int
        var found: Boolean
        var c: Char
        var foundPosition: Int
        var endedInHead = false
        var limitReached = false
        var prevRead = 0;
        val bufferSizeBefore = stringBuffer.length
        ensureOpen()
        bufferLoop@ while (true) {
            var aPos = pos - historyRelativePosition
            if (aPos >= limit) {
                fillNextBuffer()
                aPos = pos - historyRelativePosition
            }
            if (aPos >= limit || limitReached) { /* EOF */

                if (resetOnFail) {
                    resetPosition(positionBefore)
                }
                return false
            }
            found = false
            c = Char(0)

            val startedInHistory = aPos < 0
            foundPosition = aPos
            startedPosition = aPos
            endedInHead = false
            prevRead = stringBuffer.length - bufferSizeBefore
            charLoop@ while (foundPosition < limit) {
                if (foundPosition < 0) {
                    c = historyBuffer[foundPosition + half]
                    endedInHead = false
                } else {
                    c = headBuffer[foundPosition]
                    endedInHead = true
                }


                if (compareChar(c, targets)) {
                    found = true
                    break@charLoop
                }


                foundPosition++

                if (readLimit > 0 && prevRead + (foundPosition - aPos) >= readLimit) {
                    limitReached = true
                    break@charLoop
                }
            }


            if (found) {
                when (matchPosition) {
                    MatchPosition.BEFORE -> {}
                    MatchPosition.AFTER -> {
                        foundPosition++
                    }
                }
            }

            if (foundPosition < pos) {
                historyRelativePosition = pos - foundPosition
            } else {
                pos = foundPosition
                historyRelativePosition = 0
            }

            if (found) {
                if (startedInHistory) {
                    if (!endedInHead) {
                        stringBuffer.append(historyBuffer, startedPosition + half, foundPosition - startedPosition)
                    } else {
                        stringBuffer.append(
                            historyBuffer,
                            startedPosition + half,
                            abs(aPos)
                        )
                        stringBuffer.append(headBuffer, 0, foundPosition)
                    }
                } else {
                    stringBuffer.append(headBuffer, startedPosition, foundPosition - startedPosition)
                }

                return true
            }

            if (startedInHistory) {
                stringBuffer.append(historyBuffer, startedPosition + half, abs(startedPosition))
                stringBuffer.append(headBuffer)
            } else {
                stringBuffer.append(headBuffer, startedPosition, foundPosition - startedPosition)
            }
        }

    }


    override fun readTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        nullIfNotFound: Boolean,
        readLimit: Int
    ): String? {
        if (targets.isEmpty()) return null

        val size = targets.size
        val pattern = arrayOfNulls<StrPattern>(size)
        for (i in 0 until size) {
            pattern[i] = StrPattern(targets[i])
        }
        return readTo(targets = (pattern as Array<StrPattern>), matchPosition, resetOnFail, nullIfNotFound, readLimit)
    }


    fun readTo(
        vararg targets: StrPattern,
        matchPosition: MatchPosition = MatchPosition.BEFORE,
        resetOnFail: Boolean = false,
        nullIfNotFound: Boolean = false,
        readLimit: Int = 0
    ): String? {

        if (targets.isEmpty()) {
            return if (nullIfNotFound) null else ""
        }

        val startMark = markPosition()
        val size = targets.size
        val firstChars = CharArray(size)
        val sizes = IntArray(size)
        val found = BooleanArray(size)
        val pointers = IntArray(size)

        var i = 0
        while (i < size) {
            firstChars[i] = targets[i].firstChar
            sizes[i] = targets[i].len
            found[i] = false
            pointers[i] = 0
            i++
        }

        var restReadLimit = readLimit

        var read: Int
        var ch: Char

        var finished: Boolean
        var foundAny = false
        var index: Int;
        var maxLen = 0
        val unlimited = readLimit == 0
        val readStringBuilder = StringBuilder(defaultCharBufferSize)
        var pointer: Int
        var findFirst: Boolean
        ensureOpen()
        synchronized(lock) {
            while (unlimited || restReadLimit > 0) {

                findFirst =
                    readToBuffer(
                        targets = firstChars, stringBuffer = readStringBuilder,
                        matchPosition = MatchPosition.BEFORE,
                        resetOnFail,
                        restReadLimit
                    )

                if (!findFirst) {
                    return if (nullIfNotFound) null else readStringBuilder.toString()
                }

                index = -1

                if (!unlimited) {
                    restReadLimit = readLimit - (markPosition() - startMark).toInt()
                }
                // execute KNC
                while (unlimited || restReadLimit > 0) {

                    finished = true
                    read = read()
                    if (read == -1) {
                        return if (nullIfNotFound) null else readStringBuilder.toString()
                    }
                    index++
                    if (!unlimited) restReadLimit--

                    ch = read.toChar()
                    readStringBuilder.append(ch)
                    for (i in 0 until size) {
                        pointer = pointers[i]
                        if (found[i]) continue
                        while (true) {
                            if (targets[i].string[pointers[i]] == ch) {
                                pointer = ++pointers[i]
                                // found!
                                if (pointer == sizes[i]) {
                                    foundAny = true
                                    found[i] = true
                                    if (pointer > maxLen) {
                                        maxLen = pointer
                                    }
                                    break
                                }
                                finished = false
                                break
                            } else {
                                if (pointer <= 0) {
                                    break
                                } else {
                                    finished = false
                                    var prev = pointers[i]
                                    pointers[i] = targets[i].lps[pointer - 1]
                                    pointer = pointers[i]
                                }
                            }
                        }
                    }

                    if (finished) {
                        break
                    }
                }

                if (foundAny) {

                    when (matchPosition) {
                        MatchPosition.BEFORE -> {
                            goBack(maxLen)
                            return readStringBuilder.substring(0, readStringBuilder.length - maxLen)
                        }

                        MatchPosition.AFTER -> {
                            return readStringBuilder.toString()
                        }
                    }

                } else {
                    restReadLimit -= index
                }

            }
        }

        if (resetOnFail) {
            resetPosition(startMark)
        }
        return if (nullIfNotFound) null else readStringBuilder.toString()

    }


    override fun goForwardTo(
        vararg targets: Char,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: HistoryBufferedReader.ReadLimit
    ): Boolean {
        val limit = calculateEndBufferReadLimit(readLimit) ?: return false
        return goForwardTo(targets = targets, matchPosition, resetOnFail, limit)
    }

    override fun goBackTo(
        vararg targets: Char,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        limit: Int
    ): Boolean {
        if (targets.isEmpty()) return false

        val position = markPosition()
        var count = 0
        while (limit == 0 || count++ < limit) {
            val ch = goBackAndGet() ?: break// read is slow !!
            // Check for match
            for (target in targets) {
                if (ch == target) {
                    if (matchPosition == MatchPosition.BEFORE) {
                        goBack()
                    }
                    return true
                }
            }
        }

        if (resetOnFail) {
            resetPosition(position)
        }

        return false
    }


    override fun markPosition(): Long {
        if (bufferCounter == 0) return 0
        return half.toLong() * (bufferCounter - 1) + pos - historyRelativePosition;
    }

    override fun resetPosition(markedPosition: Long) {
        ensureOpen()
        if (markedPosition < 0) {
            error("Invalid mark $markedPosition!")
        }
        val markedBufferCnt = (markedPosition / half).toInt() + 1
        val currentBufferCnt = bufferCounter
        val diff = currentBufferCnt - markedBufferCnt
        if (diff >= 2) {
            throw IOException("Position is overhead!")
        }
        if (diff < 0) {
            error("Invalid Mark $markedPosition! Mark if ahead of current position `${pos + (bufferCounter * half - 1)}`")
        }

        val positionToMove = (markedPosition % half).toInt()

        if (diff == 0) {
            historyRelativePosition = pos - positionToMove
        } else {
            historyRelativePosition = pos + half - positionToMove
        }
    }


    override fun close() {
        input.close()
        isOpen = false
    }

    @Throws(IOException::class)
    private fun ensureOpen() {
        if (!isOpen) throw IOException("Stream closed")
    }

}
