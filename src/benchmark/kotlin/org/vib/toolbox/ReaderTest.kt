package org.vib.toolbox

import org.junit.jupiter.api.BeforeAll
import org.vib.toolbox.Utils.files
import org.vib.toolbox.Utils.printInfo
import org.vib.toolbox.reader.DoubleBufferedReader
import org.vib.toolbox.reader.HistoryBufferedReader
import org.vib.toolbox.reader.ReaderWithMemory
import org.vib.toolbox.reader.StrPattern
import org.vib.toolbox.reader.TextReaderWithMemory
import java.io.InputStreamReader
import java.io.Reader
import kotlin.math.min
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class ReaderTest {

    companion object{


        const val attempts = 2
        val lines by lazy { files.first().bufferedReader().readLines().count() };

        @BeforeAll
        @JvmStatic
        fun createFiles(){
            Utils.createFiles()
        }


        init {
            lines
        }
    }


    private var avg: Double = 0.0;
    private var avgIterationNewLine: Double = Double.MAX_VALUE;


    @OptIn(ExperimentalTime::class)
    @Test
    fun readFilesUsingReaders(){

        // 610 mills
//        fileReader()
        bufferedReader()
        bufferedReaderReadLine()
//        textReaderWithMemory()
//        readerWithMemory()
//        bufferedReaderNew()
//        doubleBufferedReader()
//        doubleBufferedReaderRc1()
        doubleBufferedReaderRc2()
//        doubleBufferedReaderRc3()

    }

    private inline fun <reified R: Reader> readerTest(create: (InputStreamReader) -> R){
        for( i in 1 ..attempts) {
            val time = measureTimedValue {
                var text: String? = null
                for (file in files) {
                    val reader =  create(file.reader())
                    text = reader.readText()
                }
                text!!
            }

            printInfo("${R::class.java.simpleName} $i", time.duration, comparing = avg)
        }
        println()
    }

    private inline fun <reified R: HistoryBufferedReader> readerTest(create: (InputStreamReader) -> R, readLine: R.() -> String){
        for( i in 1 ..attempts) {
            val time = measureTimedValue {
                var text: String? = null
                for (file in files) {
                    val reader =  create(file.reader())
                    while (reader.hasNext()) {
                        text = reader.readLine()
                    }
                }
                text!!
            }

            printInfo("${R::class.java.simpleName} $i", time.duration, comparing = avg)
        }
        println()
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun textReaderWithMemory(){
        readerTest { TextReaderWithMemory(it.buffered()) }
        textReaderWithMemoryReadToChar()
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun readerWithMemory(){
        readerTest { ReaderWithMemory(it.buffered()) }
    }

    fun textReaderWithMemoryReadToChar(){

        for( i in 1 ..attempts) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader = TextReaderWithMemory(file.reader())
                    while (reader.readToNextOrNull('\n', inclusive = true) != null){}
                }
            }
            printInfo("READ TO char TextReaderWithMemory $i", time.duration, comparing = avg)

        }
        println()

    }



    @OptIn(ExperimentalTime::class)
    @Test
    fun bufferedReader(){

        val meaurements : MutableList<Duration> = mutableListOf()
        for( i in 1 ..attempts) {

            val time = measureTimedValue {
                var text: String? = null
                for (file in files) {
                    text = file.bufferedReader().readText()
                }
                text!!
            }
            meaurements.add(time.duration)

            printInfo("bufferedReader $i", time.duration)
        }

        avg = meaurements.drop(1).map { it.inWholeMilliseconds }.average()
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun bufferedReaderReadLine(){

        val meaurements : MutableList<Duration> = mutableListOf()
        for( i in 1 ..attempts) {
            var cnt = 0
            val time = measureTimedValue {
                var text: String? = null
                for (file in files) {
                    val reader = file.bufferedReader()
                    while (true) {
                        text = ignoreException { reader.readLine() } ?: break
                        cnt++
                    }
                }
                text!!
            }
            meaurements.add(time.duration)
            val curr = time.duration.inWholeMilliseconds.toDouble() / cnt
            avgIterationNewLine =  min(time.duration.inWholeMilliseconds.toDouble() / cnt, avgIterationNewLine)
            printInfo("bufferedReader Read line $i [$cnt] (${curr})", time.duration)
        }
        println()

    }



    @OptIn(ExperimentalTime::class)
    @Test
    fun fileReader(){

        for( i in 1 ..3) {

            val time = measureTimedValue {
                var text: String? = null
                for (file in files) {
                    text = file.readText()
                }
                text!!
            }

            printInfo("fileReader $i", time.duration, comparing = avg)
        }

    }



    @Test
    fun doubleBufferedReaderRc2(){
        if(avg == 0.0) {
            bufferedReader()
            bufferedReaderReadLine()
        }
        doubleBufferedReaderRc2REad()
        doubleBufferedReaderRc2GoBack()
        doubleBufferedReaderRc2GoBackTo()
        doubleBufferedReaderRc2GoForwardToStr()
        doubleBufferedReaderRc2GoForwardToChar()
        doubleBufferedReaderRc2ReadToChar()
        doubleBufferedReaderRc2ReadToString()

    }

    @Test
    fun doubleBufferedReaderRc2REad(){
        readerTest{ DoubleBufferedReader(it)}
    }

    fun <T> doubleBufferedReaderText(name: String, execute: DoubleBufferedReader.() -> T){

        for( i in 1 ..attempts) {
            var cnt = 0;
            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.hasNext()){
                        reader.execute()
                        cnt++
                    }
                }
            }
            val avgIterationNewLine =  time.duration.inWholeMilliseconds.toDouble() / cnt
            val iterationPers = (avgIterationNewLine / this.avgIterationNewLine * 100).toInt()
            printInfo("$name $i [$cnt $iterationPers%]", time.duration, comparing = avg)
        }
        println()
    }

    @Test
    fun doubleBufferedReaderRc2GoBack(){


        for( i in 1 ..attempts) {
            val time = measureTimedValue {
                var text: String? = null
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    text = reader.readText()
                    while (reader.goBack()){}
                }
            }

            printInfo("GO BACK DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)
        }
        println()
    }

    @Test
    fun doubleBufferedReaderRc2GoBackTo(){

        for( i in 1 ..attempts) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    text =reader.readText()
                    while (reader.goBackTo("\n")){foundCnt++}
                }
            }
            cnt = text!!.count{it -> it == '\n'}
            printInfo("GO BACK TO DoubleBufferedReaderRC2 $i ($foundCnt/$cnt) [${(foundCnt*100)/cnt}% history coverage]", time.duration, comparing = avg)

        }
        println()

    }

    @Test
    fun doubleBufferedReaderRc2GoForwardToStr(){

        doubleBufferedReaderText("GO FORWARD TO string(2) DoubleBufferedReaderRC2"){
            goForwardTo(" >", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }
        doubleBufferedReaderText("GO FORWARD TO string(4) DoubleBufferedReaderRC2"){
            goForwardTo("   >", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }

        doubleBufferedReaderText("GO FORWARD TO string(2, 4) DoubleBufferedReaderRC2"){
            goForwardTo(" \n", " >", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }

    }


    @Test
    fun doubleBufferedReaderRc2GoForwardToChar(){

        if(avg == 0.0){
            bufferedReader()
            bufferedReaderReadLine()
        }

        doubleBufferedReaderText("GO FORWARD TO char(1) DoubleBufferedReaderRC2"){
            goForwardTo('\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }

        doubleBufferedReaderText("GO FORWARD TO char(4) DoubleBufferedReaderRC2"){
            goForwardTo('\r', '\t', '%', '\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }
        doubleBufferedReaderText("GO FORWARD TO char(4) LIMITED DoubleBufferedReaderRC2"){
            goForwardTo('\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER, readLimit = HistoryBufferedReader.ReadLimit.END_OF_BUFFER)
        }

    }


    @Test
    fun doubleBufferedReaderRc2ReadToChar(){
        var text = ""
        doubleBufferedReaderText("READ TO char DoubleBufferedReaderRC2"){
            text = readTo('\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER)!!
        }
    }


    @Test
    fun doubleBufferedReaderRc2ReadToString(){

        if(avg == 0.0){
            bufferedReader()
            bufferedReaderReadLine()
        }

        var text = ""

        val pattern = StrPattern(" >")
        doubleBufferedReaderText("READ TO 1 String(1) DoubleBufferedReaderRC2"){
            text = readTo(" >", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)!!
        }

        doubleBufferedReaderText("READ TO 1 Pattern String(1) DoubleBufferedReaderRC2"){
            text = readTo(pattern, matchPosition = HistoryBufferedReader.MatchPosition.AFTER)!!
        }

        doubleBufferedReaderText("READ TO String(1) DoubleBufferedReaderRC2"){
            text = readTo(" >", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)!!
        }
//
//        doubleBufferedReaderText("READ TO String(2) DoubleBufferedReaderRC2"){
//            text = readTo(" >", ">.", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)!!
//        }
//
//        doubleBufferedReaderText("READ TO String(5) DoubleBufferedReaderRC2"){
//            text = readTo("\n ", " >", ">.", "> ", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)!!
//        }




    }



    @Test
    fun doubleBufferedReaderRc2GoForwardToCharTest(){

        bufferedReader()
        println()
        bufferedReaderReadLine()
        println()

//        doubleBufferedReaderText("Read Line DoubleBufferedReaderRC2"){
//            readLine('\n')
//        }

        doubleBufferedReaderText("READ TO char(1) DoubleBufferedReaderRC2"){
            readTo('\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }
        doubleBufferedReaderText("READ TO char(3) DoubleBufferedReaderRC2"){
            readTo('a','b','\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }
        doubleBufferedReaderText("READ TO Str DoubleBufferedReaderRC2"){
            readTo(" >", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)
        }
    //        doubleBufferedReaderRc2GoForwardToChar1()

    }


}