package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.reader.ReaderWithMemory
import com.systema.kotlin.toolbox.reader.TextReaderWithMemory
import org.junit.jupiter.api.Disabled
import java.io.File
import java.io.FileInputStream
import java.io.Reader
import java.io.StringWriter
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.charset.Charset
import java.nio.file.StandardOpenOption
import kotlin.concurrent.thread
import kotlin.test.Test

@Disabled
class ReaderBenchmark {


    val file = File("./src/test/resources/eqc-9632.log")
    val file2 = File("./src/test/resources/eqc-9633.log")
    val file3 = File("./src/test/resources/eqc-9634.log")

    private fun printMemory() : Double{
        val diff = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).toDouble() / 1024 / 1024
        println("MB: $diff");
        return diff
    }

    @Test
    fun readFileByBufferedReader(){
        for (i in 1 .. 30){
            val r = file.bufferedReader()
            r.readText()
            r.close()
            Thread.sleep(500)
            printMemory()
        }
    }

    @Test
    fun readFileByBufferedReaderByBuffer(){
        for (i in 1 .. 30){
            val r = file.bufferedReader()
            val c = CharArray(20000)
            r.read(c)
            r.close()
            Thread.sleep(500)
            printMemory()
        }
    }

    @Test
    fun reader(){
        for (i in 1 .. 30){
            val r = file.reader()
            val buff = CharArray(1024)
            while (r.read(buff) != -1) { }
            r.close()
            Thread.sleep(500)
            printMemory()
        }
    }




    @Test
    fun bufferedReader(){
        for (i in 1 .. 30){
            val r = file.bufferedReader()
            val buff = CharArray(1024)
            while (r.read(buff) != -1) { }
            r.close()
            Thread.sleep(500)
            printMemory()
        }
    }

    @Test
    fun textReaderWithMemory(){
        for (i in 1 .. 30){
            val before = printMemory()
            val buff = CharArray(1024)
            val r = TextReaderWithMemory(file.bufferedReader())
            while (r.read(buff) != -1) { }
            r.close()
            val after = printMemory()
            println("DIFF: " + (before - after))
            println()
            Thread.sleep(500)
        }
    }

    @Test
    fun readerWithMemory(){
        for (i in 1 .. 30){
            val before = printMemory()
            val buff = CharArray(1024)
            val r = ReaderWithMemory(file.bufferedReader())
            while (r.read(buff) != -1) { }
            r.close()
            val after = printMemory()
            println("DIFF: " + (before - after))
            println()
            Thread.sleep(500)
        }
    }

    @Test
    fun channel(){
        for (i in 1 .. 30){
            val before = printMemory()
            val rbc: ReadableByteChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
            val buff = ByteBuffer.allocate(1)
            while (rbc.read(buff) > 1) { }
            rbc.close()
            val after = printMemory()
            println("DIFF: " + (before - after))
            println()
            Thread.sleep(500)
        }
    }


    @Test
    fun readFileByTextReaderWithMemory(){
        for (i in 0 .. 10) {
            val before = printMemory()
            val r = TextReaderWithMemory(file.bufferedReader())
            val stringWriter = StringWriter()
            r.copyTo(stringWriter, file.length().toInt())
            val file = stringWriter.toString()
            stringWriter.flush()
            val after = printMemory()
            println("DIFF: " + (after - before).round(2))
            println("File Len: " + (file.byteInputStream().readBytes().size.toDouble() / 1024 / 1014).round(2) + " Mb")
            println(file.takeLast(100).trim())
            println("READ char:" + file.length)
            println()
            Thread.sleep(500)
        }

    }


    fun readFileByChannel(f: File, bufSize: Int): String {
        val channel: ReadableByteChannel =  FileInputStream(f).channel
        val buf = ByteBuffer.allocateDirect(bufSize)
        val sb = StringWriter()
        var bytesRead = 0
        while (bytesRead >= 0) {
            bytesRead = channel.read(buf)
            buf.flip()
            for (i in 0 until bytesRead) {
                val b = buf.get()
                sb.append(Char(b.toUShort()))
            }
        }
        channel.close()
        return sb.toString()
    }

    fun readFileByChannelInputStreamReader(f: File, bufSize: Int): String {
        val channel: ReadableByteChannel =  FileInputStream(f).channel
        val reader = Channels.newInputStream(channel).reader()
        val text = reader.readText()
        reader.close()
        channel.close()
        return text
    }

    fun readFileByChannelReader(f: File, bufSize: Int): String {
        val channel: ReadableByteChannel =  FileInputStream(f).channel
        val reader = Channels.newReader(channel, Charset.forName("UTF-8").newDecoder(), bufSize)
        val text = reader.readText()
        reader.close()
        channel.close()
        return text
    }

    @Test
    fun readFileByChannel(){
        val before = printMemory()
        println()
        for (i in 1 .. 30){
            val before = printMemory()
            val text = readFileByChannel(file, 10000)
            val after = printMemory()
            println("DIFF: " + (after - before))
            println("File Len: " + (file.readBytes().size.toDouble() / 1024 / 1014).round(2) + " Mb")
            println(text.takeLast(100).trim())
            println("READ chars:" + text.length)
            println()
            Thread.sleep(1000)
        }
    }

    @Test
    fun readFileByChannelInputStreamReader(){
        val before = printMemory()
        println()
        for (i in 1 .. 30){
            val before = printMemory()
            readFileByChannelInputStreamReader(file, 10000)
            val after = printMemory()
            println("DIFF: " + (after - before))
            println()
            Thread.sleep(500)
        }
    }

    @Test
    fun readFileByChannelReader(){
        val before = printMemory()
        println()


        thread {
            for (i in 1..30) {
                val before = printMemory()
                readFileByChannelReader(file, 10000)
                val after = printMemory()
                println("DIFF: " + (after - before))
                println()
                Thread.sleep(500)
                System.gc()
            }
        }

        thread {
            for (i in 1..30) {
                val before = printMemory()
                readFileByChannelReader(file3, 10000)
                val after = printMemory()
                println("DIFF: " + (after - before))
                println()
                Thread.sleep(500)
                System.gc()
            }
        }

        for (i in 1..30) {
            val before = printMemory()
            readFileByChannelReader(file2, 10000)
            val after = printMemory()
            println("DIFF: " + (after - before))
            println()
            Thread.sleep(500)
            System.gc()
        }
    }

    @Test
    fun channelReader(){
        val before = printMemory()
        println()
        val buff = ByteArray(100)

        for (i in 1 .. 30){
            val sb = StringBuilder()
            val before = printMemory()
            val rbc: ReadableByteChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
            val reader = Channels.newInputStream(rbc)
            while (reader.read(buff) != -1) {
//                sb.append(buff.toString(Charset.forName("UTF-8")))
            }
            rbc.close()
            System.gc()
            val after = printMemory()
            println("DIFF: " + (after - before))
            println()
            Thread.sleep(1000)
//            println(sb.toString().substring(0,100))
        }
        println(buff.joinToString(""){ Char(it.toInt()).toString() })
    }

    @Test
    fun textReaderWithMemoryBasedOnChannelReader(){
        val before = printMemory()
        println()
        val arr = CharArray(100)
        for (i in 1 .. 30){
            val before = printMemory()
            val rbc: ReadableByteChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
            val reader = TextReaderWithMemory(Channels.newInputStream(rbc).reader())
            while (reader.read(arr) > 0) { }
            rbc.close()
            val after = printMemory()
            println("DIFF: " + (after - before))
            println()
            Thread.sleep(500)
        }
        println(arr.last())
    }

    @Test
    fun readerWithMemoryBasedOnChannelReader(){
        val before = printMemory()
        println()
        val arr = CharArray(100)
        for (i in 1 .. 30){
            val before = printMemory()
            val rbc: ReadableByteChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
            val reader = ReaderWithMemory(Channels.newInputStream(rbc).reader())
            while (reader.read(arr) > 0) { }
            rbc.close()
            val after = printMemory()
            println("DIFF: " + (after - before))
            println()
            Thread.sleep(500)
        }
        println(arr.last())
    }

}