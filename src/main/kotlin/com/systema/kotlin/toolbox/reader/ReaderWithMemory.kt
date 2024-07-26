package com.systema.kotlin.toolbox.reader

import com.systema.kotlin.toolbox.collections.LinkedArray
import java.io.BufferedReader
import java.io.Reader

open class ReaderWithMemory: BiReader, BiDirectionalReader {
    protected val buffer : LinkedArray<Int>
    private val inReader: Reader

    override val currentPositionFromLastRead  get() = buffer.currentPositionFromLastRead.toLong()
    val currentPositionFromFirstReadBuffered  get() = buffer.currentPositionFromFirstRead
    override val currentPositionFromFirstRead : Long get() {
        if(lastElementAchived) return -1
        return readCnt - buffer.currentPositionFromLastRead -1
    }
    var readCnt: Long = 0
    private set;

    private var lastElementAchived = true

    companion object{

        val defaultBufferSize = 8012;

        fun ofBuffered(reader: Reader, bufferLen: Int = defaultBufferSize): ReaderWithMemory {
            return ReaderWithMemory(BufferedReader(reader), bufferLen)
        }
    }

    constructor(reader: Reader, bufferLen: Int = defaultBufferSize) : super(reader){
        buffer = LinkedArray(bufferLen)
        this.inReader = reader;
    }

    /**
     * Read Buffer from next position to end and fill the array
     * @return numbers of read elements
     */
    private fun readBufferTo(cbuf: CharArray, off: Int, len: Int): Int{

        var i = 0
        goNext()
        while (i != len){
            cbuf[i+off] = Char(buffer.getCurrent()!!)
            i++;
            if(buffer.hasNext()){
                if(i != len) {
                    buffer.goNext()
                }
            }
            else{
                break
            }

        }

        return i
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {

        var readFromBuffer = minOf(Math.max(buffer.currentPositionFromLastRead, 0), len)


        if(readFromBuffer == 0 && lastElementAchived && buffer.isNotEmpty()){
            readFromBuffer = 1;
        }

        if(readFromBuffer > 0){
            readBufferTo(cbuf, off, readFromBuffer)
        }


        val read =  inReader.read(cbuf, off+readFromBuffer, len-readFromBuffer)
        if(read == -1) return read
        for (i in 0 until read){
            addToReadBuffer(cbuf[i])
        }
        readCnt += read

        if(lastElementAchived){
            if(readFromBuffer == 1){
                buffer.goToFirstRead()
            }
            lastElementAchived = false
        }

        return read + readFromBuffer;
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
        if(lastElementAchived) return -1 // expected read(..)
        return buffer.getCurrent() ?: -1
    }

    override fun goToFirstRead(): Boolean {
        lastElementAchived = true
        return buffer.goToFirstRead()
    }

    override fun goToLastRead(): Boolean {
        lastElementAchived = false
        return buffer.goToLastRead()
    }


    override fun getFirstReadAndMove() : Int{
        buffer.goToFirstRead()
        return buffer.getCurrent() ?: -1;
    }

    override fun goBack(): Boolean{
        val res =  buffer.goBack()
        if(!lastElementAchived && !res) {
            lastElementAchived = true
            return true;
        }
        return res
    }

    override fun getPrevAndMove(): Int{
        return getPrevAndMove(1)
    }

    override fun getPrevAndMove(steps: Int): Int{
        if(!buffer.goBack(minOf(steps, buffer.size))) return -1
        return buffer.getCurrent()!!
    }



    override fun getLastRead(): Int{
        buffer.markPosition();
        val el = getLastReadAndMove()
        buffer.moveToMarkedPosition()
        return el;
    }


    override fun getFirstRead() : Int{
        buffer.markPosition();
        val el = getFirstReadAndMove()
        buffer.moveToMarkedPosition()
        return el;
    }

    override fun getPrev(): Int{
        return getPrev(1)
    }

    override fun getPrev(steps: Int): Int{
        if(!buffer.hasPrev()) return -1
        buffer.markPosition();
        val el = getPrevAndMove(steps)
        buffer.moveToMarkedPosition()
        return el;
    }

    override fun getNext() : Int {
        if(!hasNext())  return -1
        buffer.markPosition()
        val v = getNextAndMove() ?: -1
        buffer.moveToMarkedPosition()
        return v
    }

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
        return getFromCurrentPositionTo(0)
    }

    private fun getFromCurrentPositionTo(positionFromLastRead: Int): CharArray {
        val size = buffer.currentPositionFromLastRead + 1 - positionFromLastRead
        val array = CharArray( size )
        lastElementAchived = false
        for (i in 0 until  size ){
            array[i] = buffer.getCurrent()!!.toChar()
            goNext();
        }
        if(buffer.currentPositionFromLastRead != positionFromLastRead) {
            goBack()
        }
        return array
    }


    override fun goBackTo(vararg char: Char, inclusive: Boolean): Boolean {
        while (buffer.hasPrev()){
            buffer.goBack()
            if(char.contains(buffer.getCurrent()!!.toChar())) {
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

    fun goToNextChar(char: Char, include: Boolean = false): Boolean{
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

        // read next
        while (true){
            val c = read()
            if(c == -1) break
            if(code == c){
                if(!include){
                    goBack()
                }
                return true
            }
        }
        return false
    }

    override fun goToNext(vararg char: Char, inclusive: Boolean): Boolean{
        if(char.size == 1){
            return goToNextChar(char[0], inclusive)
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
        while (true){
            val c = read()
            if(c == -1) break
            if(char.contains(c.toChar())){
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
        return getFromCurrentPositionTo(0)
    }

    override fun goBackToCharAndGetAllToLastRead(vararg char: Char, inclusive: Boolean): CharArray {
        return goBackToCharAndGetAllToLastRead(*char,  include = inclusive, nullIfNotFound = false)!!
    }

    override fun goBackToCharAndGetAllToLastReadOrNull(vararg char: Char, inclusive: Boolean): CharArray?{
        return goBackToCharAndGetAllToLastRead(*char,  include = inclusive, nullIfNotFound = true)
    }

    private fun readToNextIncludedOrEnd( nullIfNotFound: Boolean, includeCurrent: Boolean, includeLast: Boolean, vararg char: Char): CharArray? {
        val currentPositionBefore = currentPositionFromFirstRead

        val found = goToNext(*char, inclusive = includeLast)

        if(!found && nullIfNotFound) return null

        // positions to shift
        val puffDif = currentPositionFromFirstRead - currentPositionBefore

        // nothing to return
        if(currentPositionBefore == currentPositionFromFirstRead) {
            return if(nullIfNotFound) null
            else CharArray(0)
        }

        val currentPositionFromLastRead = currentPositionFromLastRead

        // if exclude current byte -> select next position from begin to get bytes
        goBack((puffDif - 1).toInt())
        if(includeCurrent) goBack()

        // end position
        val positionFromLastRead : Int

        if(!found){
            // read to the end
            positionFromLastRead = 0
        }
        else{
            // currentPositionFromLastRead is already include or exclude last element
            positionFromLastRead = currentPositionFromLastRead.toInt()
        }

        return getFromCurrentPositionTo(positionFromLastRead)
    }

    override fun readToNextIncludingCurrentOrNull(vararg char: Char, inclusive: Boolean): CharArray? {
        return readToNextIncludedOrEnd(true, true, inclusive, *char)
    }

    override fun readToNextIncludingCurrent(vararg char: Char, inclusive: Boolean): CharArray {
        return readToNextIncludedOrEnd(false, true, inclusive, *char)!!
    }

    override fun readToNext(vararg char: Char, inclusive: Boolean): CharArray {
        return readToNextIncludedOrEnd( false, false,  inclusive, *char)!!
    }

    override fun readToNextOrNull(vararg char: Char, include: Boolean): CharArray? {
        return readToNextIncludedOrEnd( true, false, include, *char)
    }

    override fun hasPrevious(): Boolean {
        return buffer.hasPrev()
    }

    override fun hasNext(): Boolean {
        if(buffer.hasNext()) return true
        val r = read()
        goBack()
        return r != -1
    }

    override fun hasCurrent(): Boolean {
        return getCurrent() != -1
    }

    override fun goNext(): Boolean {
        // when reader returns to first position
        // next read should return first postion
        if(lastElementAchived){
            if(buffer.isNotEmpty()){
                buffer.goToFirstRead() // do not move from firs position
                lastElementAchived = false
                return true
            }
        }
        return buffer.goNext()
    }

    override fun getFromFirstReadToCurrent(): CharArray {
        buffer.markPosition()
        val array: CharArray = CharArray(buffer.currentPositionFromFirstRead + 1)
        buffer.goToFirstRead()
        for (i in array.indices){
            array[i] = buffer.getCurrent()!!.toChar()
            goNext();
        }
        buffer.moveToMarkedPosition()
        return array

    }


}