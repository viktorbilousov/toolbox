package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.reader.BiReader
import com.systema.kotlin.toolbox.reader.ReaderWithMemory
import com.systema.kotlin.toolbox.reader.TextReaderWithMemory
import com.systema.kotlin.toolbox.reader.readToLineBreakTrimmed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.StringReader

class TextMemoryReaderTest: BiReaderTest(){

    override fun createReader(text: String, buffer: Int): TextReaderWithMemory {
        return TextReaderWithMemory(StringReader(text), buffer)
    }

    override fun createReader(text: String): TextReaderWithMemory {
        return super.createReader(text) as TextReaderWithMemory
    }


    private fun CharArray.asText() : String {
        return String(this, 0, this.size)
    }

    @Test
    fun getCurrentLineCntDoesNotMovePointer(){
        val TEXT = "1234|56789|123"
        val reader = createReader(TEXT)
        reader.currentPositionFromFirstRead shouldBe -1L
        reader.currentLineCnt shouldBe 1
        reader.currentPositionFromFirstRead shouldBe -1L
    }

    @Test
    fun getCurrentLineCntDoesNotMovePointerAfterReset(){
        val TEXT = "1234|56789|123\n1234|56789|123"
        val reader = createReader(TEXT)

        val position = reader.markPosition()
        reader.currentPositionFromFirstRead shouldBe -1L
        reader.readToLineBreakTrimmed() shouldBe "1234|56789|123"
        reader.currentPositionFromFirstRead shouldBe 13L
        
        reader.reset(position)
        reader.currentPositionFromFirstRead shouldBe -1
        reader.currentLineCnt shouldBe 1
        reader.currentPositionFromFirstRead shouldBe -1

    }



}