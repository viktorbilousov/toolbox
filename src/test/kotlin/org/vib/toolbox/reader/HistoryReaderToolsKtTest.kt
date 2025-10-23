package org.vib.toolbox.reader

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HistoryReaderToolsKtTest {

    private fun readerOf(text: String, capacity: Int = 8192 * 2) = HistoryBufferedReader(text.reader(), capacity)


    @Test
    fun skipSpaces() {
        val string = "    text    ";
        val reader = readerOf(string)
        reader.skipSpaces()
        reader.peekNext() shouldBe 't'
        reader.readText() shouldBe "text    "
    }

    @Test
    fun `skipSpaces case 2`() {
        val string = "123    text";
        val reader = readerOf(string)
        reader.skipSpaces()
        reader.peekNext() shouldBe '1'
        reader.readText() shouldBe string
    }

    @Test
    fun `skipSpaces case 4`() {
        val string = "1    text";
        val reader = readerOf(string)
        reader.skipSpaces()
        reader.peekNext() shouldBe '1'
        reader.readText() shouldBe string
    }

    @Test
    fun `skipSpaces case 3`() {
        val string = "   text    123";
        val reader = readerOf(string)
        reader.skipSpaces()
        reader.peekNext() shouldBe 't'
        reader.goForward(4)
        reader.skipSpaces()
        reader.readText()  shouldBe "123"
    }

    @Test
    fun goBackToLineBegin() {
        val str = "aaaa\nbbbb\ncccc\ndddd"
        val reader = readerOf(str)
        reader.readText()

        reader.getFromFirstReadToCurrent() shouldBe "aaaa\nbbbb\ncccc\ndddd"

        reader.goBackToLineBegin() shouldBe true
        reader.getFromFirstReadToCurrent() shouldBe "aaaa\nbbbb\ncccc\n"

        reader.goBack()
        reader.goBackToLineBegin() shouldBe true
        reader.getFromFirstReadToCurrent() shouldBe "aaaa\nbbbb\n"

        reader.goBack()
        reader.goBackToLineBegin() shouldBe true
        reader.getFromFirstReadToCurrent() shouldBe "aaaa\n"

        reader.goBack()
        reader.goBackToLineBegin() shouldBe true
        reader.getFromFirstReadToCurrent() shouldBe ""


        reader.goBackToLineBegin() shouldBe true

    }


    @Test
    fun goBackToLineBegin_not_found_end() {
        val str = "1234567890"
        val reader = readerOf(str,4)
        reader.readText()

        reader.getFromFirstReadToCurrent() shouldBe "7890"

        reader.goBackToLineBegin() shouldBe false
        reader.getFromFirstReadToCurrent() shouldBe ""
    }


    @Test
    fun `should read to line break n`(){
        val str = "aaaa\nbbbb\ncccc\ndddd"
        val reader = readerOf(str)

        reader.readToLineBreak() shouldBe "aaaa"
        reader.readToLineBreak() shouldBe "bbbb"
        reader.readToLineBreak() shouldBe "cccc"
        reader.readToLineBreak() shouldBe "dddd"
    }

    @Test
    fun `should read to line break rn`(){
        val str = "aaaa\r\nbbbb\r\ncccc\r\ndddd"
        val reader = readerOf(str)

        reader.readToLineBreak() shouldBe "aaaa"
        reader.readToLineBreak() shouldBe "bbbb"
        reader.readToLineBreak() shouldBe "cccc"
        reader.readToLineBreak() shouldBe "dddd"
    }


}