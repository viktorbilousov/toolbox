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

    /**
     *
     * go back to a char (one from *char array).
     * if inclusive == true, next read() return fisrt character after found
     * else read() returns second charactedf the first character after found one.
     *            in this case use getCurrent() to get found character
     *
     *
     *
     * for example :
     *
     *   val TEXT = "12345|6789123"
     *    val reader = createReader(TEXT)
     *    reader.readText()
     *    reader.goBackTo('|', include = false) shouldBe true
     *    reader.getCurrent().toChar() shouldBe '6'
     *    reader.read().toChar() shouldBe '7'
     *
     *    reader.goToLastRead()
     *    reader.goBackTo('|', include = true) shouldBe true
     *    reader.getCurrent().toChar() shouldBe '|'
     *    reader.read().toChar() shouldBe '6'
     *
     *  @param inclusive if true -> current character == found character, else current is next to found
     *  @param char - characters to find
     *  @return true if character is found
     */
    fun goBackTo(vararg char: Char, inclusive: Boolean = false): Boolean

    /**
     * read to next char (one from *char array).
     * if inclusive == false, next read() return back first find character
     * else read() returns next the first character after found one
     *
     *
     * for example :
     *
     *  val TEXT = "1234|56789|123"
     *  val reader = createReader(TEXT)
     *  reader.goToNext('|', include = false)
     *  reader.getCurrentChar() shouldBe '4'
     *  reader.readChar() shouldBe '|'
     *
     *  reader.goToFirstRead()
     *  reader.goToNext('|', include = true)
     *  reader.getCurrentChar() shouldBe '|'
     *  reader.readChar() shouldBe '5'
     *
     * @param inclusive if true -> current character == found character, else current is prev to found
     * @param char - characters to find
     * @return true if character is found
     */
    fun goToNext(vararg char: Char, inclusive: Boolean = false): Boolean


    fun goBackToCharAndGetAllToLastRead(vararg char: Char, inclusive: Boolean = false): CharArray
    fun goBackToCharAndGetAllToLastReadOrNull(vararg char: Char, inclusive: Boolean = false): CharArray?
    fun getFromFirstReadToCurrent(): CharArray


    fun readToNext(vararg char: Char, inclusive: Boolean = false): CharArray
    fun readToNextOrNull(vararg char: Char, include: Boolean = false): CharArray?
    fun readToNextIncludingCurrent(vararg char: Char, inclusive: Boolean = false): CharArray
    fun readToNextIncludingCurrentOrNull(vararg char: Char, inclusive: Boolean = false): CharArray?
}