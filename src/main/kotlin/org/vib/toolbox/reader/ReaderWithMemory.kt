package org.vib.toolbox.reader

import org.vib.toolbox.collections.LinkedArray
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import kotlin.math.min

open class ReaderWithMemory: BiReader, BiDirectionalReader {
    protected val buffer : LinkedArray<Int>
    private val inReader: Reader
    private val internalReadBufferSize = 1;
    /**
     *
     *                [a, b, c, d]
     *   firstRead -1  0  1  2  3
     *   lastRead   4  3  2  1  0
     */
    override val currentPositionFromLastRead : Long get() {
        if (!buffer.hasCurrent() && buffer.isEndReached){
            // you need to read one step
            return buffer.currentPositionFromLastRead.toLong() + 1
        }
        return buffer.currentPositionFromLastRead.toLong()
    }

    /**
     *                [a, b, c, d]
     *   values    -1  0  1  2  3
     *   readCnt    4  4  4  4  4
     *   lastRead   4  3  2  1  0
     */
    override val currentPositionFromFirstRead : Long get() {
        if(!buffer.hasCurrent()) {
            if(!buffer.isEndReached) {
                return -1
            }
            else {
                return readCnt - buffer.currentPositionFromLastRead - 2
            }
        }
        // last = readCnt(4) - bufferLastReadPosition(0) - 1 = 3
        // first = readCnt(4) - bufferLastReadPosition(0)

        return readCnt - buffer.currentPositionFromLastRead - 1
    }

    var readCnt: Long = 0
    private set

    private val bufferLen : Int

    companion object{

        val defaultBufferSize = 8012

        fun ofBuffered(reader: Reader, bufferLen: Int = defaultBufferSize): ReaderWithMemory {
            return ReaderWithMemory(BufferedReader(reader), bufferLen)
        }
    }

    constructor(reader: Reader, bufferLen: Int = defaultBufferSize) : super(reader){
        buffer = LinkedArray(bufferLen)
        this.inReader = reader
        this.bufferLen = bufferLen
    }

    /**
     * Read Buffer from next position to end and fill the array
     * @return numbers of read elements
     */
    private fun readBufferTo(cbuf: CharArray, off: Int, len: Int): Int{

        var i = 0

        while (i != len && goNext()){

            cbuf[i+off] = buffer.getCurrent()!!.toChar()
            i++

        }

        return i
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {

        /**
         * when currentPositionFromLastRead > 0, some values are buffered and need to be added in a output array
         *
         * for example
         *
         * buffer   1 1 1 1 2 3 4 5
         * current            X
         * lastread 6 7 5 4 3 2 1 0
         *
         * currentPositionFromLastRead = 2
         * output array ( 4, 5 , ... read from stream)
         *
         */
        val readFromBuffer = minOf(buffer.currentPositionFromLastRead, len)


        if (readFromBuffer > 0) {
            readBufferTo(cbuf, off, readFromBuffer)
        }


        // read rest from stream
        val read =  inReader.read(cbuf, off+readFromBuffer, len-readFromBuffer)
        if(read == -1) {
            if(readFromBuffer == 0) return -1
            return readFromBuffer
        }

        // add new read values to buffer
        for (i in readFromBuffer until read+readFromBuffer){
            addToReadBuffer(cbuf[i])
        }

        // increase internal counter
        readCnt += read


        return read + readFromBuffer
    }

    override fun close() {
        inReader.close()
    }

    protected fun addToReadBuffer(int: Int){
        buffer.add(int)
    }

    protected fun addToReadBuffer(char: Char){
        addToReadBuffer(char.code)
    }


    override fun getLastReadAndMove(): Int{
        buffer.goToLastRead()
        return buffer.getCurrent() ?: -1
    }

    override fun getCurrent(): Int {
        return buffer.getCurrent() ?: -1
    }

    override fun goToFirstReadBuffered(): Boolean {
        return  buffer.goToFirstRead() && buffer.goBack()
    }

    override fun goToLastRead(): Boolean {
        return buffer.goToLastRead()
    }


    override fun getFirstReadAndMove() : Int{
        buffer.goToFirstRead()
        return buffer.getCurrent() ?: -1
    }

    override fun goBack(): Boolean{
        return buffer.goBack()
    }

    override fun getPrevAndMove(): Int{
        return getPrevAndMove(1)
    }

    override fun getPrevAndMove(steps: Int): Int{
        if(!buffer.goBack(minOf(steps, buffer.size))) return -1
        return buffer.getCurrent()!!
    }



    override fun getLastRead(): Int{
        buffer.markPosition()
        val el = getLastReadAndMove()
        buffer.moveToMarkedPosition()
        return el;
    }


    override fun getFirstRead() : Int{
        buffer.markPosition()
        val el = getFirstReadAndMove()
        buffer.moveToMarkedPosition()
        return el
    }

    override fun getPrev(): Int{
        return getPrev(1)
    }

    override fun getPrev(steps: Int): Int{
        if(!buffer.hasPrev()) return -1
        buffer.markPosition()
        val el = getPrevAndMove(steps)
        buffer.moveToMarkedPosition()
        return el
    }

//    override fun getNext() : Int {
//        if(!hasNext())  return -1
//        buffer.markPosition()
//        val v = getNextAndMove() ?: -1
//        buffer.moveToMarkedPosition()
//        return v
//    }

    fun getNextAndMove() : Int {
        if(!hasNext()) return -1
        goNext()
        return buffer.getCurrent() ?: -1
    }


    /**
     * Returns puffered values from buffered position
     * If current position is last read -> returns last read position
     */
    override fun getFromCurrentPositionToLastRead(): CharArray {
        return getBufferedFromCurrentPositionInclusiveTo(0)
    }


    private fun getBufferedFromCurrentPositionInclusiveTo(positionFromLastRead: Int): CharArray {

        /**
         *
         * buffer                            [  a b c d e f g ] (7 el)
         * currentPositionFromLastRead    7     6 5 4 3 2 1 0
         *
         *
         * if current position is 5 und positionFromLastRead 1 , size = 5-1 = 4
         *
         */


        // read all:
        // positionFromLastRead = 0
        // current position if 7 (need to read all)
        // 7 - 0 = 0

        // current position 6
        // size 6 - 0 + 1 = 7

        // current position 5
        // size = 5 - 0 + 1 = 6
        //        if(!buffer.onFirstPosition){
        //           size++;
        //        }

        val size = Math.min(buffer.currentPositionFromLastRead - positionFromLastRead +1, buffer.size)



        val array = CharArray( size )
        for (i in 0 until  size ){
            array[i] = buffer.getCurrentOrNext()!!.toChar()

            if(i != size-1) {
                goNext()
            }
        }

//        //?
//        if(buffer.currentPositionFromLastRead != positionFromLastRead) {
//            goBack()
//        }
        return array
    }


    override fun goBackTo(vararg char: Char, inclusive: Boolean): Boolean {
        while (buffer.hasPrev()){
            buffer.goBack()
            val curr = buffer.getCurrent()?.toChar() ?: return false
            if(char.contains(curr)) {
                if(!inclusive){
                    goNext()
                }
                return true
            }
        }
        return false
    }

//    fun goBackTo(vararg char: Char): Boolean{
//        val res = goBackToIncluded(*char)
//        if(!res) return false
//        buffer.goNext()
//        return true
//    }

    fun goToNextChar(char: Char, readLimit: Int, include: Boolean = false): Boolean{
        val code = char.code
        // find in buffer
        while (buffer.hasNext()){
            goNext()

            if(code == buffer.getCurrent()!!) {
                if(!include){
                    goBack()
                }
                return true
            }
        }

        var cnt = 0
        val internalReadBuffer = CharArray(internalReadBufferSize)
        // read next
        while (true){
            val c = read(internalReadBuffer)
            if(c == -1) break
            if(code == internalReadBuffer[0].code){
                if(!include){
                    goBack()
                }
                return true
            }

            cnt++
            if(readLimit > 0 && cnt == readLimit){
                return false
            }
        }
        return false
    }

    override fun goToNext(vararg char: Char,  inclusive: Boolean, readLimit: Int,): Boolean{
        if(char.size == 1){
            return goToNextChar(char[0], readLimit, inclusive)
        }
        while (buffer.hasNext()){
            goNext()
            if(char.contains(buffer.getCurrent()!!.toChar())) {
                if(!inclusive){
                    buffer.goBack()
                }
                return true
            }
        }
        val internalReadBuffer = CharArray(internalReadBufferSize)
        while (true){
            val c = read(internalReadBuffer)
            if(c == -1) break
            if(char.contains(internalReadBuffer[0])){
                if(!inclusive){
                    buffer.goBack()
                }
                return true
            }
        }
        return false
    }


    private fun goBackToCharAndGetAllToLastRead(vararg char: Char, include: Boolean = false, nullIfNotFound: Boolean = false): CharArray?{
        if(!goBackTo(*char, inclusive = include) && nullIfNotFound) return null
        return getBufferedFromCurrentPositionInclusiveTo(0)
    }

    override fun goBackToCharAndGetAllToLastRead(vararg char: Char, inclusive: Boolean): CharArray {
        return goBackToCharAndGetAllToLastRead(*char,  include = inclusive, nullIfNotFound = false)!!
    }

    override fun goBackToCharAndGetAllToLastReadOrNull(vararg char: Char, inclusive: Boolean): CharArray?{
        return goBackToCharAndGetAllToLastRead(*char,  include = inclusive, nullIfNotFound = true)
    }

    private fun readToNextIncludedOrEnd(nullIfNotFound: Boolean, includeCurrent: Boolean, includeLast: Boolean, readLimit: Int, vararg char: Char): CharArray? {

        val capacity  = if(readLimit > 0) readLimit else min(1000, bufferLen)
        val sb = StringBuilder(capacity)

        val maxRead = if(readLimit > 0) readLimit else Int.MAX_VALUE

        val buffer = CharArray(1)
        var found = false;

        if(includeCurrent && hasCurrent()){
            sb.append(getCurrent().toChar())
        }

        for (i in 0 until maxRead) {
            if(read(buffer) == -1) break
            val c = buffer[0]
            if(char.contains(c)){
                found = true
                if(includeLast){
                    sb.append(c)
                }
                else{
                    goBack()
                }
                break
            }
            sb.append(c)
        }

        if(!found && nullIfNotFound) return null

        val charArray = CharArray(sb.length);
        sb.toCharArray(charArray)
        return charArray

    }

    override fun readToNextIncludingCurrentOrNull(vararg char: Char,  inclusive: Boolean, readLimit: Int): CharArray? {
        return readToNextIncludedOrEnd(true, true, inclusive, readLimit, *char)
    }

    override fun readToNextIncludingCurrent(vararg char: Char,  inclusive: Boolean, readLimit: Int,): CharArray {
        return readToNextIncludedOrEnd(false, true, inclusive,  readLimit, *char)!!
    }

    override fun readToNext(vararg char: Char,  inclusive: Boolean, readLimit: Int): CharArray {
        return readToNextIncludedOrEnd( false, false,  inclusive,  readLimit, *char)!!
    }

    override fun readToNextOrNull(vararg char: Char,  inclusive: Boolean, readLimit: Int): CharArray? {
        return readToNextIncludedOrEnd( true, false,  inclusive,  readLimit, *char)
    }

    override fun hasPrevious(): Boolean {
        return buffer.hasPrev()
    }

    override fun hasNext(): Boolean {
        if(buffer.hasNext()) return true
        val internalReadBuffer = CharArray(internalReadBufferSize)
        val r = read(internalReadBuffer)
        goBack()
        return r != -1
    }

    override fun hasCurrent(): Boolean {
        return getCurrent() != -1
    }

    override fun goNext(): Boolean {
        return buffer.goNext()
    }

    override fun getFromFirstReadToCurrent(): CharArray {
        buffer.markPosition()
        val array: CharArray = CharArray(buffer.currentPositionFromFirstRead + 1)
        buffer.goToFirstRead()
        for (i in array.indices){
            array[i] = buffer.getCurrentOrNext()!!.toChar()
            goNext()
        }
        buffer.moveToMarkedPosition()
        return array

    }

    override fun markSupported(): Boolean {
        return true
    }

    private var markedPosition = -2L
    private var readAheadLimit = 0L

    override fun mark(readAheadLimit: Int) {
        var limit = readAheadLimit
        if(limit <= 0){
           limit = bufferLen
        }

        markedPosition = this.currentPositionFromFirstRead
        this.readAheadLimit = markedPosition + limit
    }

    override fun markPosition(readAheadLimit: Int): Long {
        var limit = readAheadLimit
        if(limit <= 0){
            limit = bufferLen
        }

        markedPosition = this.currentPositionFromFirstRead
        this.readAheadLimit = markedPosition + limit
        return markedPosition
    }


    protected fun markPositionInternal(readLimit: Int) : Long{
        val markedPositionBefore = markedPosition;
        val newMarkedPosition = markPosition(readLimit)
        markedPosition = markedPositionBefore
        return newMarkedPosition
    }

    protected fun markPositionInternal() : Long = markPositionInternal(bufferLen)

    override fun markPosition(): Long  = markPosition(bufferLen)

    override fun reset(markedPosition: Long) {
        if(markedPosition == -2L){
            throw IOException("Marker is not set!")
        }

        if(currentPositionFromFirstRead > readAheadLimit){
            throw IOException("Position is overhead!")
        }

        if(currentPositionFromFirstRead > markedPosition){
            goBack((currentPositionFromFirstRead - markedPosition).toInt() )
        }
        else{
            val internalReadBuffer = CharArray(internalReadBufferSize)
            while (currentPositionFromFirstRead != markedPosition){
                read(internalReadBuffer)
            }
        }
    }

    override fun reset()  = reset(markedPosition)

}