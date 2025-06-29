package org.vib.toolbox//import org.vib.toolbox.StringStreamReader2
//import io.kotest.matchers.shouldBe
//import org.junit.jupiter.api.Test
//import java.io.BufferedInputStream
//import java.io.InputStreamReader
//import java.io.StringReader
//
//class StringStreamPuferedReaderTest {
//    companion object{
//        val text = "Path for java installation 'C:\\Users\\bilousov\\.jdks\\corretto-17.0.6' (IntelliJ IDEA) does not contain a java executable"
//    }
//
//    @Test
//    fun readText(){
//        val reader = StringReader()
//
//        val stream = BufferedInputStream(StringReader(""))
//            stream
//       val reader = StringStreamReader2(text, 10);
//        reader.readText() shouldBe text
//    }
//
//
//    @Test
//    fun readBype(){
//        val reader = StringStreamReader2(text, 10);
//        val sb = StringBuilder()
//        while (reader.hasNext()){
//            sb.append(reader.readNext())
//        }
//        sb.toString() shouldBe text
//    }
//}