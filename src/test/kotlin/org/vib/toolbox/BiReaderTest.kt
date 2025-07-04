package org.vib.toolbox

import org.vib.toolbox.reader.readChar
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.vib.toolbox.reader.BiReader
import org.vib.toolbox.reader.TextReaderWithMemory
import org.vib.toolbox.reader.getCurrentChar
import org.vib.toolbox.reader.getFromCurrentPositionToLastReadAsText
import org.vib.toolbox.reader.getFromFirstReadToCurrentAsText
import org.vib.toolbox.reader.goBackToLineBegin
import org.vib.toolbox.reader.goToLineBreak
import org.vib.toolbox.reader.goToNext
import org.vib.toolbox.reader.readToLineBreak
import org.vib.toolbox.reader.readToLineBreakTrimmed
import org.vib.toolbox.reader.readToNext

open abstract class BiReaderTest {

    protected open fun createReader(text: String) : BiReader = createReader(text, text.length)
    abstract fun createReader(text: String, buffer : Int) : BiReader


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
        reader.goToFirstReadBuffered()
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


        reader.goToFirstReadBuffered()
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
    fun goToNextCase3(){
        val TEXT = "2025/05/20 01:19:21.937  - PID: 15620       - LEVEL: 4 COM       - TA: RLNYNBP0.14749532               - bus                        - CSysRvBusReceiveMessage              - Received message on subject \"RF360.SG.NA.Prod.Equipment.Resource.Event.Report.F4VCO539\" from \"SVSGHEIBUSPRD05\$/eqs/9.2.18//172.29.149.211(SVSGHEIBUSPRD05)\": <?xml version='1.0' encoding='UTF-8'?><MESSAGE><CMD type=\"A\">EVENT_REPORT</CMD><MID type=\"A\">F4VCO539</MID><MTY type=\"A\">E</MTY><TID type=\"U4\">14730509</TID><ECD type=\"U4\">30013</ECD><ETX type=\"A\">An Event that was not expected was received from the machine.</ETX><CEID type=\"A\">6005</CEID><EVENT_ID type=\"A\">6005</EVENT_ID><DATAID type=\"U4\">153875709</DATAID><CEID type=\"U4\">6005</CEID><DATE_TIME type=\"A\">2025-05-20 01:19:21+08:00</DATE_TIME></MESSAGE>\n"
        val reader = createReader(TEXT)
        //encoding='UTF-8'?>
        reader.goToNext("encoding='UTF-8'?>", inclusive = true)
        reader.getFromFirstReadToCurrentAsText() shouldBe (TEXT.substringBefore("encoding='UTF-8'?>") + "encoding='UTF-8'?>")
        reader.readToNext("</") shouldBe "<MESSAGE><CMD type=\"A\">EVENT_REPORT</"
    }

    @Test
    fun goReadToNExt(){
        val TEXT = "2025/05/20 01:19:21.937  - PID: 15620       - LEVEL: 4 COM       - TA: RLNYNBP0.14749532               - bus                        - CSysRvBusReceiveMessage              - Received message on subject \"RF360.SG.NA.Prod.Equipment.Resource.Event.Report.F4VCO539\" from \"SVSGHEIBUSPRD05\$/eqs/9.2.18//172.29.149.211(SVSGHEIBUSPRD05)\": <?xml version='1.0' encoding='UTF-8'?><MESSAGE><CMD type=\"A\">EVENT_REPORT</CMD><MID type=\"A\">F4VCO539</MID><MTY type=\"A\">E</MTY><TID type=\"U4\">14730509</TID><ECD type=\"U4\">30013</ECD><ETX type=\"A\">An Event that was not expected was received from the machine.</ETX><CEID type=\"A\">6005</CEID><EVENT_ID type=\"A\">6005</EVENT_ID><DATAID type=\"U4\">153875709</DATAID><CEID type=\"U4\">6005</CEID><DATE_TIME type=\"A\">2025-05-20 01:19:21+08:00</DATE_TIME></MESSAGE>\n"
        val reader = createReader(TEXT)
        //encoding='UTF-8'?>
        reader.readToNext("encoding='UTF-8'?>", inclusive = true) shouldBe "2025/05/20 01:19:21.937  - PID: 15620       - LEVEL: 4 COM       - TA: RLNYNBP0.14749532               - bus                        - CSysRvBusReceiveMessage              - Received message on subject \"RF360.SG.NA.Prod.Equipment.Resource.Event.Report.F4VCO539\" from \"SVSGHEIBUSPRD05\$/eqs/9.2.18//172.29.149.211(SVSGHEIBUSPRD05)\": <?xml version='1.0' encoding='UTF-8'?>"
    }


    @Test
    fun goToNextCase2(){
        val TEXT = "1234\n56789\n123"
        val reader = createReader(TEXT)
        reader.goToNext('\n', inclusive = false)
        reader.getFromFirstReadToCurrentAsText() shouldBe "1234"
        reader.getCurrentChar() shouldBe '4'
        reader.goToFirstReadBuffered()

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

        reader.goToFirstReadBuffered()
        reader.goToNext('|', inclusive = true) shouldBe true
        reader.getCurrentChar() shouldBe '|'
        reader.readChar() shouldBe '5'

        reader.goToFirstReadBuffered()
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

            reader.goToFirstReadBuffered()
            reader.goBack()

            reader.readToNext('|', inclusive = true).asText() shouldBe "1234|"

            reader.goToFirstReadBuffered()
            reader.readToNext('|', inclusive = true).asText() shouldBe "1234|"
            reader.readToNext('|').asText() shouldBe "56789"
        }

    }


    @Test
    fun goNext(){
        val TEXT = "123456789123"
        val reader = createReader(TEXT)
        reader.readText()

        reader.goToFirstReadBuffered()
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

        reader.goToFirstReadBuffered()
        reader.goToNext('|', '5', inclusive = true)
        reader.currentPositionFromFirstRead shouldBe 4
    }


    @Test
    fun readAfterGotoFirstReadReturnsFirstValue(){
        val reader = createReader("12345678")
        reader.readText()
        reader.goToFirstReadBuffered()
        reader.read().toChar() shouldBe '1'
    }


    @Test
    fun readToNextFromCurrent(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.read()
        reader.getCurrent().toChar() shouldBe '1'
        reader.readToNext('|').asText() shouldBe "234"

        reader.goToFirstReadBuffered()
        reader.goBack()
        reader.read().toChar() shouldBe '1'
        reader.readToNext('|', inclusive = true).asText() shouldBe "234|"

        reader.goToFirstReadBuffered()
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

        reader.goToFirstReadBuffered()
        reader.read()


        reader.readToNextIncludingCurrent('|', inclusive = true).asText() shouldBe "1234|"

        reader.goToFirstReadBuffered()
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


        reader.goToFirstReadBuffered()
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
        reader.currentPositionFromFirstRead shouldBe -1L
        reader.readToNext("|") shouldBe "1234|"
        reader.currentPositionFromFirstRead shouldBe 4
        reader.reset(position)
        reader.currentPositionFromFirstRead shouldBe -1L
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
    fun goToNextStr(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.goToNext("567", inclusive = false) shouldBe true
        reader.getFromFirstReadToCurrentAsText() shouldBe "1234|"
        reader.readText() shouldBe "56789|123"
    }

    @Test
    fun goToNextStrIncl(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.goToNext("567", inclusive = true) shouldBe true
        reader.getFromFirstReadToCurrentAsText() shouldBe "1234|567"
        reader.readText() shouldBe "89|123"
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
    fun readToBackLineCase1(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("789|", inclusive = true) shouldBe "1234|56789|"
        reader.goBackToLineBegin()
        reader.readText() shouldBe "1234|56789|123"
    }

    @Test
    fun readToLineBreakTrimmed(){
        val TEXT = "1234|56789|123\nabcd"
        val reader = createReader(TEXT)
        reader.readToLineBreakTrimmed() shouldBe "1234|56789|123"
        reader.readToLineBreakTrimmed() shouldBe "abcd"
    }

    @Test
    fun readToBackLineCase2(){
        val TEXT = "someline\n1234|56789|123"
        val reader = createReader(TEXT)
        reader.readToNext("789|", inclusive = true) shouldBe "someline\n1234|56789|"
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

    @Test
    fun readShouldWorksByBufferOverload(){
        val TEXT = "1234560abcdefghigk"
        val reader = createReader(TEXT, 5)

        assertSoftly {
            reader.readToNextIncludingCurrent('4', inclusive = true).asText() shouldBe "1234"
            reader.currentPositionFromFirstRead shouldBe 3L
            reader.readToNext('0', inclusive = true).asText() shouldBe "560"
            reader.currentPositionFromFirstRead shouldBe 6L
            reader.readToLineBreakTrimmed() shouldBe "abcdefghigk"
            reader.currentPositionFromFirstRead shouldBe 17L
        }
    }

    @Test
    fun goBackCase(){
        val TEXT = "1234560abcdefghigk"
        val reader = createReader(TEXT)

        assertSoftly {
            reader.readToNextIncludingCurrent('4', inclusive = true).asText() shouldBe "1234"
            reader.currentPositionFromFirstRead shouldBe 3L
            reader.readToNext('0', inclusive = true).asText() shouldBe "560"
            reader.currentPositionFromFirstRead shouldBe 6L
            reader.readToLineBreakTrimmed() shouldBe "abcdefghigk"
            reader.currentPositionFromFirstRead shouldBe 17L
        }
    }

    @Test
    fun goBackCaseToTheBeginning(){
        val TEXT = "1234560abcdefghigk\n12345"
        val reader = createReader(TEXT)

        assertSoftly {
            val str = reader.readToLineBreakTrimmed()
            str shouldBe "1234560abcdefghigk"
            reader.goBack(str.length+1)
            reader.currentPositionFromFirstRead shouldBe -1
            reader.readToLineBreakTrimmed() shouldBe "1234560abcdefghigk"
        }
    }

    @Test
    fun goBackCaseToTheBeginningViaPointer(){
        val TEXT = "1234560abcdefghigk\n12345"
        val reader = createReader(TEXT)
        val position = reader.markPosition()

        assertSoftly {
            val str = reader.readToLineBreakTrimmed()
            str shouldBe "1234560abcdefghigk"
            reader.reset(position)
            reader.currentPositionFromFirstRead shouldBe -1
            reader.getFromFirstReadToCurrentAsText() shouldBe ""
        }
    }

    @Test
    fun readToLineBrakeShouldReadNewLineButIgnoreItInResult(){
        val TEXT = "line1\nline2\nline3"
        val reader = createReader(TEXT)
        val line1 = reader.readToLineBreak(jumpToNextLine = true)
        val line2 = reader.readToLineBreak(jumpToNextLine = true)
        val line3 = reader.readToLineBreak(jumpToNextLine = true)

        line1 shouldBe "line1"
        line2 shouldBe "line2"
        line3 shouldBe "line3"
    }

    @Test
    fun readToLineBrakeShouldReadNewLineIncludingnewLinetInResult(){
        val TEXT = "line1\nline2\nline3"
        val reader = createReader(TEXT)
        val line1 = reader.readToLineBreak(jumpToNextLine = false)
        reader.read()
        val line2 = reader.readToLineBreak(jumpToNextLine = false)
        reader.read()
        val line3 = reader.readToLineBreak(jumpToNextLine = false)
        reader.read()

        line1 shouldBe "line1"
        line2 shouldBe "line2"
        line3 shouldBe "line3"
    }

    @Test
    fun readToLineBrakeShouldReadNewLine(){
        val TEXT = "line1\nline2\nline3"
        val reader = createReader(TEXT)
        val line1 = reader.readToLineBreak(jumpToNextLine = false)
        val line2 = reader.readToLineBreak(jumpToNextLine = false)
        val line3 = reader.readToLineBreak(jumpToNextLine = false)

        line1 shouldBe "line1"
        line2 shouldBe ""
        line3 shouldBe ""
    }

    @Test
    fun readToLineBrakeShouldJumbOverNewLine(){
        val TEXT = "line1\nline2\nline3"
        val reader = createReader(TEXT)
        val line1 = reader.readToLineBreak(jumpToNextLine = false)
        reader.readToLineBreak() // jump
        val line2 = reader.readToLineBreak(jumpToNextLine = false)
        reader.readToLineBreak() // jump
        val line3 = reader.readToLineBreak(jumpToNextLine = false)

        line1 shouldBe "line1"
        line2 shouldBe "line2"
        line3 shouldBe "line3"
    }


    @Test
    fun shouldNotSkipFirstCharIfGoToNext(){
        val str = "'S6F11_SC_E4050_E_4050_1 | S6F12_SC_E4050_E_4050_1_reply' 'A' 'B' 'C' 'D' 'E'"

        val texts = mutableListOf<String>()
        val reader = TextReaderWithMemory(str.trim());
        while (reader.hasNext()){
            reader.goToNext('\'', inclusive = true)
            texts.add(reader.readToNext('\'', inclusive = false).asText())
            reader.goNext()
        }

        texts shouldContainExactly listOf(
            "S6F11_SC_E4050_E_4050_1 | S6F12_SC_E4050_E_4050_1_reply",
            "A",
            "B", "C", "D", "E"
        )
    }

}