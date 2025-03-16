package org.vib.toolbox

import org.vib.toolbox.reader.TextReaderWithMemory
import java.io.InputStream
import java.io.Reader

interface IParser<EL> {

    fun parse(string: String) : EL = parse(TextReaderWithMemory(string))

    fun parse(inputStream: InputStream)  : EL

    fun parse(reader: Reader): EL

}
