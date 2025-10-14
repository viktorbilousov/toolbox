package org.vib.toolbox.reader
import org.vib.toolbox.reader.HistoryBufferedReader.MatchPosition
import java.io.IOException
import java.io.Reader
import kotlin.math.abs
import kotlin.math.min

class DoubleBufferedReader(
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
//    private var inactiveLimit = 0
    private var endReached = false
    private var isHistoryBufferEmpty = true
    private var bufferCounter = 0;

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
        if(historyRelativePosition == 0) return ""
        val absoluteposition = pos - historyRelativePosition
        if (absoluteposition < 0) {
            if (isHistoryBufferEmpty) return String(historyBuffer, 0, pos + 1)
            val sb = StringBuilder(half * 2)
            sb.append(historyBuffer, absoluteposition + half, abs(absoluteposition))
            sb.append(headBuffer, 0, pos)
            return sb.toString()
        }
        else {
            return String(headBuffer, absoluteposition, pos-absoluteposition)
        }
    }

    override fun read(): Int {

        if(historyRelativePosition > 0){
            if(pos - historyRelativePosition < 0){
                if(isHistoryBufferEmpty) return  -1;
                val index = pos - historyRelativePosition + half
                historyRelativePosition--
                return historyBuffer[index].code
            }
            else{
                val index = pos - historyRelativePosition
                historyRelativePosition--
                return headBuffer[index].code
            }
        }
        else{
            if (pos >= limit) {
                if (!fillNextBuffer()) return -1
            }
            return headBuffer[pos++].code
        }
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {

        var totalRead = 0

        while (totalRead < len) {
            // read from stream
            if (pos >= limit) {
                if (!fillNextBuffer()) break
            }
            var currentPosition = pos - historyRelativePosition

            // read from history buffer
            if(pos - historyRelativePosition < 0){
                val positionInHistoryBuffer = pos - historyRelativePosition + half
                val available = minOf(half - positionInHistoryBuffer, len - totalRead)
                historyBuffer.copyInto(cbuf, off + totalRead, positionInHistoryBuffer, positionInHistoryBuffer + available)
                historyRelativePosition -= available
                totalRead += available
                continue;
            }

            val available = minOf(limit - currentPosition, len - totalRead)
            headBuffer.copyInto(cbuf, off + totalRead, currentPosition, currentPosition + available)
            historyRelativePosition -= available
            if(historyRelativePosition < 0){
                pos -= historyRelativePosition // pos = pos - (-history) = pos + history
                historyRelativePosition = 0
            }
            totalRead += available
        }
        return if (totalRead == 0 && endReached) -1 else totalRead
    }

    override fun goBackAndGet(): Char? {
        if(!goBack()) return null
        if(!hasCurrent()) return null
        return peekCurrent()
    }

    override fun goBack(steps: Int): Boolean {
        for (i in steps downTo 0) {
            if(!goBack()) return false
        }
        return true
    }

    override fun peekPrevious(): Char? {
        if(!hasPrevious()) return null
        goBack()
        val c = peekCurrent()
        goForward()
        return c
    }

    override fun goForward(steps: Int): Boolean {
        for (i in steps downTo 0) {
            if(!goForward()) return false
        }
        return true
    }

    override fun goForward(): Boolean {
        if(historyRelativePosition != 0){
            historyRelativePosition --;
            return true
        }
        else{
            return read() != -1
        }
    }

    override fun goBackAndGet(steps: Int): Char? {
        if(!goBack(steps-1)) return null
        return goBackAndGet()
    }

    override fun peekNext(): Char? {
        if(!goForward()) return null
        val c = peekCurrent()
        goBack()
        return c
    }

    override fun hasNext(): Boolean {
        if(endReached) return false

        val absolutePosition = pos - historyRelativePosition
        // start of file
        if(limit == 0){
            val res = goForward()
            pos = 0 // reset to fist position
            return res
        }
        else if(absolutePosition == limit) {
            val res = goForward()
            pos--;
            return res
        }
        return true
    }

    override fun hasPrevious(): Boolean {
        if(limit == 0) return false
        // pos is pointer to "next to read". If pos = 1 -> first was read -> ok
        val absolutePosition = pos - historyRelativePosition - 1
        if(absolutePosition == 0 && isHistoryBufferEmpty){
            return false
        }
        if(absolutePosition + half < 0) return false
        return true
    }

    private fun canBeMovedBack(): Boolean {
        if(limit == 0) return false
        // pos is pointer to "next to read". If pos = 1 -> first was read -> ok
        val absolutePosition = pos - historyRelativePosition
        if(absolutePosition == 0 && isHistoryBufferEmpty){
            return false
        }
        if(absolutePosition + half < 0) return false
        return true
    }



    override fun goBack(): Boolean {
        // no read values
        if(!canBeMovedBack()){
            return false;
        }

        // Move backward
        historyRelativePosition ++;

        val absolutePosition = pos - historyRelativePosition

        if(absolutePosition < 0){
            // check in history buffer
            if(!isHistoryBufferEmpty && absolutePosition + half >= 0){
                return true
            }
            else{
                // go forward to first read character
                historyRelativePosition++
                return false;
            }
        }
        else{
            return true
        }
    }

    override fun peekCurrent(): Char? {
        // pos is pointer to "next to read", so current will be pos-1
        try {
            var position = pos - 1 - historyRelativePosition
            if(position < 0){
                if(isHistoryBufferEmpty) return null
                position += half
                return historyBuffer[position]
            }
            return headBuffer[position]
        }catch (e: Exception){
            throw e
        }

    }



    override fun hasCurrent() : Boolean{
        if(limit == 0)  return false
        // pos is pointer to "next to read", so current will be pos-1
        if(pos - historyRelativePosition -1 < 0 && isHistoryBufferEmpty) return false
        return true
    }


    override fun goBackTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean ,
        limit: Int
    ): Boolean {
        val targets = targets.filter { it.isNotEmpty() }
        if (targets.isEmpty()) return false
        if(targets.all { it.length == 1 }){
            val arr = targets.map { it[0] }.toCharArray()
            return goBackTo(targets = arr, matchPosition= matchPosition, resetOnFail = resetOnFail, limit = limit)
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
                            MatchPosition.BEFORE -> { /* already before match */ goBack() }
                            MatchPosition.AFTER -> repeat(target.length-1) { read() }
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
        if(limit != 0){
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

    private fun calculateEndBufferReadLimit(readLimit: HistoryBufferedReader.ReadLimit): Int?{
        if(readLimit == HistoryBufferedReader.ReadLimit.END_OF_BUFFER){
            if(limit == 0){
                if(!hasNext()) return null
            }
            val absolutePosition = pos - historyRelativePosition

            // end of the buffer
            if(absolutePosition == limit){
                return half
            }

           if(isHistoryBufferEmpty){
               return half*2 - absolutePosition
           }
           else {
               return half - absolutePosition
           }
        }
        else{
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
        return goForwardTo(targets = targets, matchPosition, resetOnFail,  limit)
    }

    override fun goForwardTo(
        vararg targets: String,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: Int
    ): Boolean {
        val targets = targets.filter { it.isNotEmpty() }
        if (targets.isEmpty()) return false
        if(targets.all { it.length == 1 }){
            val arr = targets.map { it[0] }.toCharArray()
            return goForwardTo(targets = arr, matchPosition= matchPosition, resetOnFail = resetOnFail, readLimit = readLimit)
        }

        val position = markPosition()
        val maxTargetLen = targets.maxOf { it.length }

        val window = CharArray(maxTargetLen)
        var windowSize = 0
        var count = 0

        while (readLimit == 0 || count++ < readLimit) {
            val ch = read()
            if(ch == -1) break;

            // Add character to sliding window
            if (windowSize < maxTargetLen) {
                window[windowSize++] = ch.toChar()
            } else {
                // Shift left
                for (i in 0 until maxTargetLen - 1) window[i] = window[i + 1]
                window[maxTargetLen - 1] = ch.toChar()
            }

            // Check for match
            for (target in targets) {
                if (windowSize >= target.length) {
                    var matched = true
                    for (i in target.indices) {
                        // Compare most recent chars
                        if (window[windowSize - target.length + i] != target[i]) {
                            matched = false
                            break
                        }
                    }

                    if (matched) {
                        when (matchPosition) {
                            MatchPosition.BEFORE -> {
                                // Step back to just before match
                                repeat(target.length) { goBack() }
                            }
                            MatchPosition.AFTER -> {
                                // Already after match → do nothing
                            }
                        }
                        return true
                    }
                }
            }
        }

        if (resetOnFail) {
           resetPosition(position)
        }
        return false
    }

    fun goForwardTo1(
        vararg targets: String,
        matchPosition: MatchPosition = MatchPosition.BEFORE,
        resetOnFail: Boolean = false,
        readLimit: Int = 0
    ): Boolean {


        val startMark = markPosition()
        val maxTargetLen = targets.maxOf { it.length }

        // Use a circular buffer
        val window = CharArray(maxTargetLen)
        var windowSize = 0
        var writeIndex = 0
        var readCount = 0

        // Precompute first character of each target for faster matching
        val firstChars = targets.map { it.first() }

        while (readLimit == 0 || readCount++ < readLimit) {
            val chInt = read()
            if (chInt == -1) break
            val ch = chInt.toChar()

            window[writeIndex] = ch
            if (windowSize < maxTargetLen) windowSize++
            writeIndex = (writeIndex + 1) % maxTargetLen

            // Only bother checking if new char could start any target
            if (ch !in firstChars) continue

            // Check each target efficiently in circular buffer
            for (target in targets) {
                val tLen = target.length
                if (windowSize < tLen) continue

                var matched = true
                for (i in 0 until tLen) {
                    val bufIndex = (writeIndex - tLen + i + maxTargetLen) % maxTargetLen
                    if (window[bufIndex] != target[i]) {
                        matched = false
                        break
                    }
                }

                if (matched) {
                    when (matchPosition) {
                        MatchPosition.BEFORE -> {
                            // Move back before the target
                            repeat(tLen) { goBack() }
                        }
                        MatchPosition.AFTER -> {
                            // already after → do nothing
                        }
                    }
                    return true
                }
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

        val position = markPosition()
        var count = 0
        while (readLimit == 0 || count++ < readLimit) {
            val ch = read().toChar() // read is slow ??
            if (ch == '\uFFFF') break;
            // Check for match
            for (target in targets) {
                if (ch == target) {
                    if(matchPosition ==  MatchPosition.BEFORE){
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




    override fun readTo(
        vararg targets: Char,
        matchPosition: MatchPosition,
        resetOnFail: Boolean,
        readLimit: Int
    ): String? {

        if(targets.isEmpty()) return null

        val markedPosition = markPosition()

        val expectedSize = half*2
        val sb = StringBuilder(expectedSize)
        var counter = 0;
        var failed = true;
        while (readLimit == 0 || counter < readLimit){
            var positionBeforeReading = pos - historyRelativePosition
            if(positionBeforeReading == limit){
                positionBeforeReading = 0;
            }

            val safeLimit = calculateEndBufferReadLimit(HistoryBufferedReader.ReadLimit.END_OF_BUFFER) ?: break
            val limit =  if(readLimit == 0) safeLimit else min(readLimit, safeLimit)
            val isEmptyBefore = isHistoryBufferEmpty
            // go to the end of current buffer
            val foundInCurrentBuffer = goForwardTo(targets = targets,
                matchPosition = matchPosition,
                resetOnFail = false,
                readLimit = limit
                )


            var positionAfterReading = pos - historyRelativePosition

            if(!isHistoryBufferEmpty && isEmptyBefore){
                positionBeforeReading -= half
            }


            if(positionBeforeReading < 0 && !isHistoryBufferEmpty){
                val startPositionInHistory = positionBeforeReading + half
                sb.append(historyBuffer, startPositionInHistory, abs(positionBeforeReading))
                sb.append(headBuffer, 0, positionAfterReading)
            }
            else{
                sb.append(headBuffer, positionBeforeReading, positionAfterReading - positionBeforeReading)
            }

            if(foundInCurrentBuffer){
                failed = false
                break
            }
            else{
                // if not found -> read limit elements
                counter += positionAfterReading - positionBeforeReading
            }

            if(endReached){
                break
            }
        }

        if(failed){
            if(resetOnFail){
                resetPosition(markedPosition)
            }
            sb.clear()
            return null
        }

        return sb.toString()
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
                    if(matchPosition ==  MatchPosition.BEFORE){
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

    //    fun goForwardTo1(
//        vararg targets: Char,
//        matchPosition: MatchPosition = MatchPosition.BEFORE,
//        resetOnFail: Boolean = false,
//        readLimit: Int = 0
//    ): Boolean {
//        if (targets.isEmpty()) return false
//
//        val position = markPosition()
//        var count = 0
//        // first check history
//        if(position - historyRelativePosition < 0){
//            var buffer = historyBuffer
//            val pos = position - historyRelativePosition + half
//            for (i in pos .. (half-1)){
//                if()
//            }
//
//        }
//
//
//        while (readLimit == 0 || count++ < readLimit) {
//            if(read(buffer, 0 , 1) == -1) break
//            val ch = buffer[0]
//            // Check for match
//            for (target in targets) {
//                if (ch == target) {
//                    if(matchPosition ==  MatchPosition.BEFORE){
//                        goBack()
//                    }
//                    return true
//                }
//            }
//        }
//
//        if (resetOnFail) {
//            resetPosition(position)
//        }
//
//        return false
//    }




    override fun markPosition(): Long {
        if(bufferCounter == 0) return 0
        return half.toLong()*(bufferCounter-1) + pos-historyRelativePosition;
    }

    override fun resetPosition(markedPosition: Long) {

        if(markedPosition < 0){
            error("Invalid mark $markedPosition!")
        }
        val markedBufferCnt = (markedPosition / half).toInt() + 1
        val currentBufferCnt = bufferCounter
        val diff = currentBufferCnt - markedBufferCnt
        if(diff >= 2){
            throw IOException("Position is overhead!")
        }
        if(diff < 0){
            error("Invalid Mark $markedPosition! Mark if ahead of current position `${pos + (bufferCounter*half-1)}`")
        }

        val positionToMove = (markedPosition % half).toInt()

        if(diff == 0){
            historyRelativePosition =  pos - positionToMove
        }
        else {
            historyRelativePosition =  pos + half - positionToMove
        }
    }


    override fun close() {
        input.close()
    }
}
