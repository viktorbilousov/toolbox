package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.reader.BiReader
import com.systema.kotlin.toolbox.reader.ReaderWithMemory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.io.StringReader

class ReaderWithMemoryTest: BiReaderTest(){

    override fun createReader(text: String): BiReader {
       return ReaderWithMemory.ofBuffered(StringReader(text))
    }


    private fun CharArray.asText() : String {
        return String(this, 0, this.size)
    }

    @Test
    fun shouldGetCurrentReadPositionWhenBufferIsFull(){
        val TEXT = "123456789"
        val reader = ReaderWithMemory.ofBuffered(StringReader(TEXT), 5)
        reader.readText() shouldBe TEXT
        reader.currentPositionFromFirstRead shouldBe  8
        reader.currentPositionFromLastRead shouldBe 0
        reader.readCnt shouldBe 9

        reader.goBack()
        reader.currentPositionFromFirstRead shouldBe 7
        reader.currentPositionFromLastRead shouldBe 1
        reader.readCnt shouldBe 9


        reader.goBack()
        reader.currentPositionFromFirstRead shouldBe 6
        reader.currentPositionFromLastRead shouldBe 2
        reader.readCnt shouldBe 9


        reader.goToFirstRead()
        reader.currentPositionFromFirstReadBuffered shouldBe 0
        reader.currentPositionFromFirstRead shouldBe -1
        reader.currentPositionFromLastRead shouldBe 4
        reader.readCnt shouldBe 9


        reader.goToLastRead()
        reader.currentPositionFromFirstRead shouldBe 8
        reader.currentPositionFromLastRead shouldBe 0
        reader.readCnt shouldBe 9
    }

}