package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.reader.*
import com.systema.kotlin.toolbox.reader.readChar
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

open abstract class BiReaderTest {

    abstract fun createReader(text: String) : BiReader


    companion object{
        private const val TEXT = "Path for java installation 'C:\\Users\\bilousov\\.jdks\\corretto-17.0.6' (IntelliJ IDEA) does not contain a java executable"
    }


    private fun CharArray.asText() : String {
        return String(this, 0, this.size)
    }

    @Test
    fun shouldRememberText(){
        val reader = createReader(TEXT)
        reader.readText() shouldBe TEXT
        reader.goToFirstRead()
        reader.getFromCurrentPositionToLastRead().asText() shouldBe TEXT
    }

    @Test
    fun shouldPreviousText(){
        val TEXT = "123456789"
        val reader = createReader(TEXT)
        reader.readText() shouldBe TEXT
        reader.getCurrent() shouldBe '9'.code
        reader.getPrevAndMove() shouldBe '8'.code
        reader.getPrev() shouldBe '7'.code
        reader.getCurrent() shouldBe '8'.code
    }

    @Test
    fun shouldGetCurrentReadPosition(){
        val TEXT = "123456789"
        val reader = createReader(TEXT)
        reader.readText() shouldBe TEXT
        reader.currentPositionFromFirstRead shouldBe 8
        reader.currentPositionFromLastRead shouldBe 0

        reader.goBack()
        reader.currentPositionFromFirstRead shouldBe 7
        reader.currentPositionFromLastRead shouldBe 1


        reader.goBack()
        reader.currentPositionFromFirstRead shouldBe 6
        reader.currentPositionFromLastRead shouldBe 2


        reader.goToFirstRead()
        reader.goBack()
        reader.currentPositionFromFirstRead shouldBe -1
        reader.currentPositionFromLastRead shouldBe 9


        reader.goToLastRead()
        reader.currentPositionFromFirstRead shouldBe 8
        reader.currentPositionFromLastRead shouldBe 0
    }


    @Test
    fun `getFromFirstReadToCurrent should not move current position`(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.goToNext('|', inclusive = false)
        reader.getCurrentChar() shouldBe '4'
        reader.getFromFirstReadToCurrentAsText() shouldBe "1234"
        reader.getCurrentChar() shouldBe '4'
    }


    @Test
    fun goToNextCase2(){
        val TEXT = "1234\n56789\n123"
        val reader = createReader(TEXT)
        reader.goToNext('\n', inclusive = false)
        reader.getFromFirstReadToCurrentAsText() shouldBe "1234"
        reader.getCurrentChar() shouldBe '4'
        reader.goToFirstRead()

        reader.goToNext('\n', inclusive = true)
        reader.getCurrentChar() shouldBe '\n'
        reader.getFromFirstReadToCurrentAsText() shouldBe "1234\n"
    }


    @Test
    fun goToNext(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.goToNext('|', inclusive = false) shouldBe true
        reader.getCurrentChar() shouldBe '4'
        reader.readChar() shouldBe '|'

        reader.goToFirstRead()
        reader.goToNext('|', inclusive = true) shouldBe true
        reader.getCurrentChar() shouldBe '|'
        reader.readChar() shouldBe '5'

        reader.goToFirstRead()
        reader.goToNext('|', inclusive = true) shouldBe true
        reader.goToNext('|')
        reader.getCurrent().toChar() shouldBe '9'
        reader.readChar() shouldBe '|'

    }


    @Test
    fun goBackToCharCase(){
        val TEXT = "12345|6789123"
        val reader = createReader(TEXT)
        reader.readText()
        reader.goBackTo('|', inclusive = false) shouldBe true
        reader.getFromCurrentPositionToLastReadAsText() shouldBe "6789123"

        reader.goToLastRead()
        reader.goBackTo('|', inclusive = true) shouldBe true
        reader.getFromCurrentPositionToLastReadAsText() shouldBe "|6789123"
    }


    @Test
    fun goBackToChar(){
        val TEXT = "12345|6789123"
        val reader = createReader(TEXT)
        reader.readText()
        reader.goBackTo('|', inclusive = false) shouldBe true
        reader.getCurrent().toChar() shouldBe '6'
        reader.read().toChar() shouldBe '7'

        reader.goToLastRead()
        reader.goBackTo('|', inclusive = true) shouldBe true
        reader.getCurrent().toChar() shouldBe '|'
        reader.read().toChar() shouldBe '6'
    }




    @Test
    fun goBackToChars(){
        val TEXT = "12345|6789123"
        val reader = createReader(TEXT)
        reader.readText()
        reader.goBackTo('|', '5') shouldBe true
        reader.getCurrent().toChar() shouldBe '6'
        reader.read().toChar() shouldBe '7'

        reader.goToLastRead()
        reader.goBackTo('|' ,'5', inclusive = true) shouldBe true
        reader.getCurrent().toChar() shouldBe '|'
        reader.read().toChar() shouldBe '6'
    }

    @Test
    fun readToNext(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        assertSoftly {

            reader.readToNext('|', inclusive = false).asText() shouldBe "1234"
            reader.readChar() shouldBe '|'
            reader.readToNext('|').asText() shouldBe "56789"

            reader.goToFirstRead()
            reader.goBack()

            reader.readToNext('|', inclusive = true).asText() shouldBe "1234|"

            reader.goToFirstRead()
            reader.readToNext('|', inclusive = true).asText() shouldBe "1234|"
            reader.readToNext('|').asText() shouldBe "56789"
        }

    }


    @Test
    fun goNext(){
        val TEXT = "123456789123"
        val reader = createReader(TEXT)
        reader.readText()

        reader.goToFirstRead()
        reader.goBack()
        reader.getCurrent() shouldBe  -1
        reader.goNext() shouldBe true
        reader.getCurrent().toChar() shouldBe '1'
        reader.goNext() shouldBe true
        reader.getCurrent().toChar() shouldBe '2'
        reader.goNext() shouldBe true
        reader.getCurrent().toChar() shouldBe '3'

    }

    @Test
    fun goPrev(){
        val TEXT = "1234|56789|abc"
        val reader = createReader(TEXT)
        reader.readText() shouldBe TEXT

        reader.goBackTo('|', inclusive = false)
        reader.getCurrent().toChar() shouldBe 'a'
        reader.readChar() shouldBe 'b'

        reader.goToLastRead()

        reader.goBackTo('|', inclusive = true)
        reader.getCurrent().toChar() shouldBe '|'

        reader.goToLastRead()
        reader.goBackTo('|', inclusive = true)
        reader.goBackTo('|')
        reader.getCurrent().toChar() shouldBe '5'

    }


    @Test
    fun  goBackToCharAndGetAllToLastRead() {
        val TEXT = "1234|56789|abc"
        val reader = createReader(TEXT)
        reader.readText() shouldBe TEXT
        reader.goBackToCharAndGetAllToLastRead('|').asText() shouldBe "abc"

        reader.goToLastRead()

        reader.goBackToCharAndGetAllToLastRead('|', inclusive = true).asText() shouldBe "|abc"

        reader.goToLastRead()
        reader.goBackTo('|', inclusive = true)
        reader.goBackToCharAndGetAllToLastRead('|').asText() shouldBe "56789|abc"
    }


    @Test
    fun  goBackToCharAndGetAllToLastReadIfNotFound() {
        val TEXT = "123456789abc"
        val reader = createReader(TEXT)
        reader.readText() shouldBe TEXT
        reader.goBackToCharAndGetAllToLastRead('|').asText() shouldBe TEXT
        reader.goBackToCharAndGetAllToLastReadOrNull('|') shouldBe null
    }

    @Test
    fun readToNextToEnd(){
        val TEXT = "123456789abc"
        val reader = createReader(TEXT)
        reader.readToNext('|').asText() shouldBe TEXT
        reader.readToNextOrNull('|') shouldBe null
    }


    @Test()
    fun readOneByOne(){
        val TEXT = "< A test 'hello' > * comment"
        val readText = StringBuilder()
        val reader = createReader(TEXT)

        while (reader.hasNext()){
            readText.append(reader.readChar())
        }

        readText.toString() shouldBe TEXT
    }

    @Test()
    fun hasNextDoNotBreakReadingOneByOneSimple(){
        val TEXT = "1234"
        val reader = createReader(TEXT)

        reader.hasNext()
        reader.readChar() shouldBe '1'
        reader.hasNext()
        reader.readChar() shouldBe '2'
        reader.hasNext()
        reader.readChar() shouldBe '3'
        reader.hasNext()
        reader.readChar() shouldBe '4'

    }

    @Test()
    fun readOneByOneSimple(){
        val TEXT = "1234"
        val reader = createReader(TEXT)

        reader.readChar() shouldBe '1'
        reader.readChar() shouldBe '2'
        reader.readChar() shouldBe '3'
        reader.readChar() shouldBe '4'
    }



    @Test()
    fun readFirst(){
        val TEXT = "12"
        val reader = createReader(TEXT)

        reader.getCurrent() shouldBe -1
        reader.read().toChar() shouldBe '1'
        reader.getCurrent().toChar() shouldBe '1'
        reader.read().toChar() shouldBe '2'
    }

    @Test()
    fun readFirstAndBack(){
        val TEXT = "12"
        val reader = createReader(TEXT)
        assertSoftly {

            reader.getCurrent() shouldBe -1
            reader.read().toChar() shouldBe '1'
            reader.getCurrent().toChar() shouldBe '1'
            reader.getCurrent().toChar() shouldBe '1'

            reader.goBack() shouldBe true
            reader.goBack() shouldBe false
            reader.getCurrent() shouldBe -1
            reader.read().toChar() shouldBe '1'
            reader.getCurrent().toChar() shouldBe '1'

            reader.goBack() shouldBe true
            reader.goBack() shouldBe false
            reader.getCurrent() shouldBe -1
            reader.read().toChar() shouldBe '1'
            reader.getCurrent().toChar() shouldBe '1'



            reader.goBack() shouldBe true
            reader.getCurrent() shouldBe -1
            reader.read().toChar() shouldBe '1'
            reader.read().toChar() shouldBe '2'
            reader.getCurrent().toChar() shouldBe '2'

            reader.goBack() shouldBe true
            reader.getCurrent().toChar() shouldBe '1'

            reader.goBack() shouldBe true
            reader.getCurrent() shouldBe -1

            reader.goBack() shouldBe false
        }
    }


    @Test
    fun getFromFirstReadToCurrent(){
        val reader = createReader(TEXT)
        reader.readText()
        reader.getFromFirstReadToCurrent().asText() shouldBe TEXT
    }


    @Test
    fun goToNextManyChars(){
        val reader = createReader("1234|5678")
        reader.goToNext('|', '5')
        reader.currentPositionFromFirstRead shouldBe 3

        reader.goToFirstRead()
        reader.goToNext('|', '5', inclusive = true)
        reader.currentPositionFromFirstRead shouldBe 4
    }


    @Test
    fun readAfterGotoFirstReadReturnsFirstValue(){
        val reader = createReader("12345678")
        reader.readText()
        reader.goToFirstRead()
        reader.read().toChar() shouldBe '1'
    }


    @Test
    fun readToNextFromCurrent(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.read()
        reader.getCurrent().toChar() shouldBe '1'
        reader.readToNext('|').asText() shouldBe "234"

        reader.goToFirstRead()
        reader.goBack()
        reader.read().toChar() shouldBe '1'
        reader.readToNext('|', inclusive = true).asText() shouldBe "234|"

        reader.goToFirstRead()
        reader.readToNext('|', inclusive = true).asText() shouldBe "1234|"
        reader.readToNext('|').asText() shouldBe "56789"
    }


    @Test
    fun readToNextIncludingCurrent(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.read()
        reader.getCurrent().toChar() shouldBe '1'
        reader.readToNextIncludingCurrent('|').asText() shouldBe "1234"

        reader.goToFirstRead()
        reader.read()


        reader.readToNextIncludingCurrent('|', inclusive = true).asText() shouldBe "1234|"

        reader.goToFirstRead()
        reader.readToNextIncludingCurrent('|', inclusive = true).asText() shouldBe "1234|"
        reader.readToNextIncludingCurrent('|').asText() shouldBe "|56789"
    }


    @Test
    fun currentPositionFromFirstRead(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.currentPositionFromFirstRead shouldBe -1
        reader.read()
        reader.currentPositionFromFirstRead shouldBe 0
        reader.read()
        reader.currentPositionFromFirstRead shouldBe 1
        reader.read()
        reader.currentPositionFromFirstRead shouldBe 2


        reader.goToFirstRead()
        reader.goBack()
        reader.currentPositionFromFirstRead shouldBe -1
        reader.read()
        reader.currentPositionFromFirstRead shouldBe 0
        reader.read()
        reader.currentPositionFromFirstRead shouldBe 1
        reader.read()
        reader.currentPositionFromFirstRead shouldBe 2

    }

    @Test
    fun readText(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readText() shouldBe TEXT
    }

    @Test
    fun readTextFromMiddle(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("|")
        reader.readText() shouldBe "56789|123"
    }

    @Test
    fun readNextDoesNotBreakReadAll(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("|", false)
        reader.readText() shouldBe "|56789|123"
    }


    @Test
    fun readTextDoesNotBreakGetCurrent(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("|", false)
        reader.readText() shouldBe "|56789|123"
        reader.getCurrentChar() shouldBe '3'
    }


    @Test
    fun markPosition(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        val str =  reader.readToNext("|", false)
        reader.markPosition()
        val positionBefore = reader.currentPositionFromFirstRead
        println(reader.currentPositionFromFirstRead)
        str shouldBe "1234"

        val readAll = reader.readText()
        readAll shouldBe "|56789|123"


        reader.reset()
        val positionAfter = reader.currentPositionFromFirstRead
        println(reader.currentPositionFromFirstRead)

        positionBefore shouldBe positionAfter


        reader.readText() shouldBe "|56789|123"
    }

    @Test
    fun markFirstPosition(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        val position = reader.markPosition()
        position shouldBe -1L
        reader.readText() shouldBe TEXT
        reader.reset(position)
        reader.readText() shouldBe TEXT
    }

    @Test
    fun readToNextStr(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("567", inclusive = false) shouldBe "1234|"
        reader.readText() shouldBe "56789|123"
    }

    @Test
    fun readToNextStringIncl(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("567", inclusive = true) shouldBe "1234|567"
        reader.readText() shouldBe "89|123"
    }

    @Test
    fun goBackToLineBegin(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("567")
        reader.readText() shouldBe "89|123"
    }

    @Test
    fun readToNextLine(){
        val TEXT = "1234|56789|123\nnewline"
        val reader = createReader(TEXT)
        reader.goToLineBreak()
        reader.readText() shouldBe "newline"
    }

    @Test
    fun readToBackLine(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("789|", inclusive = true) shouldBe "1234|56789|"
        reader.goBackToLineBegin()
        reader.readText() shouldBe "1234|56789|123"
    }

    @Test
    fun readToNextWithLimit(){
        val TEXT = "123456789|123"
        val reader = createReader(TEXT)
        reader.readToNext('|', readLimit = 3, inclusive = true).asText() shouldBe "123"
    }
    @Test
    fun readToNextStringWithLimit(){
        val TEXT = "123456789|123"
        val reader = createReader(TEXT)
        reader.readToNext(text = "9|", readLimit = 3, inclusive = true) shouldBe "123"
    }



}