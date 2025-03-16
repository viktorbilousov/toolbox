package org.vib.toolbox

import org.vib.toolbox.reader.BiReader
import org.vib.toolbox.reader.ReaderWithMemory
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.StringReader

class ReaderWithMemoryTest: BiReaderTest(){

    override fun createReader(text: String): BiReader {
       return ReaderWithMemory.ofBuffered(StringReader(text))
    }

    override fun createReader(text: String, buffer: Int): BiReader {
        return ReaderWithMemory.ofBuffered(StringReader(text), buffer)
    }


    private fun CharArray.asText() : String {
        return String(this, 0, this.size)
    }

    @Test
    fun shouldGetCurrentReadPositionWhenBufferIsFull(){
        val TEXT = "123456789"
        val reader = ReaderWithMemory.ofBuffered(StringReader(TEXT), 5)
        assertSoftly {
            // [x] - buffered
            //                           *
            // Start   1 2 3 4 [ 5 6 7 8 9 ]
            reader.readText() shouldBe TEXT
            reader.currentPositionFromFirstRead shouldBe 8
            reader.currentPositionFromLastRead shouldBe 0
            reader.readCnt shouldBe 9

            //                         *
            // Start   1 2 3 4 [ 5 6 7 8 9 ]
            reader.goBack()
            reader.currentPositionFromFirstRead shouldBe 7
            reader.currentPositionFromLastRead shouldBe 1
            reader.readCnt shouldBe 9


            //                       *
            // Start   1 2 3 4 [ 5 6 7 8 9 ]
            reader.goBack()
            reader.currentPositionFromFirstRead shouldBe 6
            reader.currentPositionFromLastRead shouldBe 2
            reader.readCnt shouldBe 9


            //                   *
            // Start   1 2 3 4 [ 5 6 7 8 9 ]
            reader.goToFirstReadBuffered()
            reader.currentPositionFromFirstRead shouldBe 3
            reader.currentPositionFromLastRead shouldBe 5
            reader.readCnt shouldBe 9


            //                           *
            // Start   1 2 3 4 [ 5 6 7 8 9 ]
            reader.goToLastRead()
            reader.currentPositionFromFirstRead shouldBe 8
            reader.currentPositionFromLastRead shouldBe 0
            reader.readCnt shouldBe 9
        }
    }

}