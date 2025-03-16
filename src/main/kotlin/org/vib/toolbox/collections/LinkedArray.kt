package org.vib.toolbox.collections

import java.util.Arrays

class LinkedArray<E>(private val capability: Int) : Collection<E> {

    constructor() : this(1000)

    override var size: Int = 0
    private set;

    private val array: Array<Any?> = Array(capability) {null}

    private var firstElementPointer = -1;
    private var lastElementPointer = -1;
    private var readPointer = -1
    private var markPosition = -1;
    var isEndReached = false
    private set;


    val onFirstPosition : Boolean = readPointer == -1

    /**
     * How many element need to read to get last element
     * 0 -> the current element is actual
     */
    val currentPositionFromLastRead: Int get()  {
        val diff =  lastElementPointer - readPointer
        // diff < 0 when size == capability
        if(diff < 0) return diff + size
        return diff
    }



    /**
     *
     *                [a, b, c, d]
     *   firstRead -1  0  1  2  3
     *   lastRead   4  3  2  1  0
     */
    val currentPositionFromFirstRead: Int get() {
        if(readPointer == -1) return -1;
        val diff = readPointer - firstElementPointer
        // diff < 0 when size == capability
        if(diff < 0) return diff + size
        return diff
    }


    init {
        require(capability > 1)
    }

    override fun contains(element: E): Boolean {
        return array.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        for (element in elements) {
            if(!array.contains(element)){
                return false
            }
        }
        return true
    }

    override fun isEmpty(): Boolean {
       return size == 0
    }

    override fun iterator(): Iterator<E> {
        return toList().iterator()
    }

    fun addAll(collection: Collection<E>){
        for (e in collection) {
            add(e)
        }
    }


    fun addAll(vararg collection: E){
        for (e in collection) {
            add(e)
        }
    }

    fun add(element: E){
        if(size == capability){
            lastElementPointer = (lastElementPointer + 1) % capability
            firstElementPointer = (firstElementPointer + 1) % capability
            readPointer = (readPointer + 1) % capability
            isEndReached = true
        }
        else{
            size++
            lastElementPointer++
            readPointer ++;
            if(size == 1){
                firstElementPointer = 0
            }
        }

        array[lastElementPointer] = element
    }

    fun clear(){
        Arrays.fill(array, null)
        size = 0
        lastElementPointer = -1
        firstElementPointer = -1
        readPointer = 0
    }

    fun getCurrentOrNext() : E?{
        if(!hasCurrent()) goNext()
        return getCurrent()
    }

   fun getCurrent(): E? {
       if(!hasCurrent()) return null
       return array[readPointer] as? E
   }


    fun goNext(): Boolean{
        if(!hasNext()) return false
        if(readPointer == -1) {
            readPointer = firstElementPointer
        }
        else {
            readPointer = (readPointer + 1) % capability
        }
        return true
    }

    fun goNext(steps: Int): Boolean{
        for (i in 1 .. steps){
            if(!goNext()) return false
        }
        return true
    }


    fun goBack(): Boolean{
        if(!hasPrev()){
            return false
        }
        if(readPointer == firstElementPointer){
            readPointer = -1
        }
        else if(readPointer == 0) {
            readPointer = size - 1
        }
        else{
            readPointer--
        }
        return true
    }


    fun goBack(steps: Int): Boolean {
        for (i in 1 .. steps){
            if(!goBack()) return false
        }
        return true
    }

    fun goToFirstRead() : Boolean {
        if(isEmpty()) return false
        readPointer = firstElementPointer
        return true
    }


    fun markPosition(){
        markPosition = readPointer
    }

    fun moveToMarkedPosition(){
        if(markPosition == -1) {
            goToLastRead()
            return
        }
        readPointer = markPosition
    }

    fun gotoReadPositionFromLastRead(position : Int){
        goToLastRead()
        goBack(position)
    }

    fun goToLastRead(): Boolean {
        if(isEmpty()) return false
        readPointer = lastElementPointer
        return true
    }

    fun toList() : List<E>{
        val list = mutableListOf<E>();
        for (i in firstElementPointer until capability){
            list.add(array[i] as E)
            if(i == lastElementPointer) {
                return list
            }
        }

        for (i in 0 .. lastElementPointer){
            list.add(array[i] as E)
        }

        return list
    }

    fun hasNext(): Boolean{
        return !isEmpty() && (readPointer != lastElementPointer)
    }

    fun hasCurrent(): Boolean{
        return !isEmpty() && readPointer != -1
    }

    fun hasPrev(): Boolean{
        return !isEmpty() && readPointer != -1
    }

    override fun toString(): String {
        return "LinkedArray(array=${array.contentToString()}, capability=$capability, firstElementPointer=$firstElementPointer, lastElementPointer=$lastElementPointer, markPosition=$markPosition, readPointer=$readPointer, size=$size, isEndReached=$isEndReached, currentPositionFromFirstRead=$currentPositionFromFirstRead, currentPositionFromLastRead=$currentPositionFromLastRead, onFirstPosition=$onFirstPosition)"
    }


}