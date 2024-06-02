package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.reader.BiReader
import com.systema.kotlin.toolbox.reader.StringBiReader

class StringBiReaderTest: BiReaderTest(){


    private fun CharArray.asText() : String {
        return String(this, 0, this.size)
    }

    override fun createReader(text: String): BiReader {
        return StringBiReader(text)
    }

}