package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.reader.TextReaderWithMemory
import java.io.InputStream
import java.io.Reader
import java.io.StringReader

interface IParser<EL> {

    fun parse(string: String) : EL = parse(TextReaderWithMemory(string))

    fun parse(inputStream: InputStream)  : EL

    fun parse(reader: Reader): EL

}
