package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.reader.BiReader
import com.systema.kotlin.toolbox.reader.ReaderWithMemory
import com.systema.kotlin.toolbox.reader.TextReaderWithMemory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.StringReader

class TextMemoryReaderTest: BiReaderTest(){

    override fun createReader(text: String): BiReader {
       return TextReaderWithMemory(text)
    }


    private fun CharArray.asText() : String {
        return String(this, 0, this.size)
    }

}