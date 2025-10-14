package org.vib.toolbox

import org.junit.jupiter.api.BeforeAll
import org.vib.toolbox.Utils.files
import org.vib.toolbox.Utils.printInfo
import org.vib.toolbox.reader.ReaderWithMemory
import org.vib.toolbox.reader.TextReaderWithMemory
import org.vib.toolbox.reader.DoubleBufferedReader
import org.vib.toolbox.reader.HistoryBufferedReader
import java.io.InputStreamReader
import java.io.Reader
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class ReaderTest {

    companion object{

        @BeforeAll
        @JvmStatic
        fun createFiles(){
            Utils.createFiles()
        }
    }


    private var avg: Double = 0.0;


    @OptIn(ExperimentalTime::class)
    @Test
    fun readFilesUsingReaders(){

        // 610 mills
//        fileReader()
        bufferedReader()
        textReaderWithMemory()
//        readerWithMemory()
//        bufferedReaderNew()
//        doubleBufferedReader()
//        doubleBufferedReaderRc1()
        doubleBufferedReaderRc2()
//        doubleBufferedReaderRc3()

    }

    private inline fun <reified R: Reader> readerTest(create: (InputStreamReader) -> R){
        for( i in 1 ..10) {
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

        for( i in 1 ..10) {
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
        for( i in 1 ..10) {

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
        }
        doubleBufferedReaderRc2REad()
        doubleBufferedReaderRc2GoBack()
        doubleBufferedReaderRc2GoBackTo()
        doubleBufferedReaderRc2GoForwardToStr2()
        doubleBufferedReaderRc2GoForwardToStr3()
//        doubleBufferedReaderRc2GoForwardToStr1()
        doubleBufferedReaderRc2GoForwardToChar()
        doubleBufferedReaderRc2GoForwardToChar4()
        doubleBufferedReaderRc2GoForwardToCharLimited()
        doubleBufferedReaderRc2ReadToChar()
    }

    @Test
    fun doubleBufferedReaderRc2REad(){

        readerTest{ DoubleBufferedReader(it)}


    }

    @Test
    fun doubleBufferedReaderRc2GoBack(){


        for( i in 1 ..10) {
            val time = measureTimedValue {
                var text: String? = null
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    text =reader.readText()
                    while (reader.goBack()){}
                }
            }

            printInfo("GO BACK DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)

        }

    }

    @Test
    fun doubleBufferedReaderRc2GoBackTo(){

        for( i in 1 ..10) {
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

    fun doubleBufferedReaderRc2GoForwardToStr2(){

        for( i in 1 ..10) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.goForwardTo("\n ", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)){}
                }
            }
            printInfo("GO FORWARD TO string(2) DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)

        }
        println()

    }

    fun doubleBufferedReaderRc2GoForwardToStr3(){

        for( i in 1 ..10) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.goForwardTo("\n   ", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)){}
                }
            }
            printInfo("GO FORWARD TO string(4) DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)

        }
        println()

    }

    fun doubleBufferedReaderRc2GoForwardToChar(){

        for( i in 1 ..10) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.goForwardTo('\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER)){}
                }
            }
            printInfo("GO FORWARD TO char(1) DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)

        }
        println()

    }

    fun doubleBufferedReaderRc2GoForwardToChar4(){

        for( i in 1 ..10) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.goForwardTo('\r', '\t', '%', '\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER)){}
                }
            }
            printInfo("GO FORWARD TO char(4) DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)

        }
        println()

    }

    fun doubleBufferedReaderRc2GoForwardToCharLimited(){

        for( i in 1 ..10) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.hasNext()){
                        reader.goForwardTo('\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER, readLimit = HistoryBufferedReader.ReadLimit.END_OF_BUFFER)
                    }
                }
            }
            printInfo("GO FORWARD TO char DoubleBufferedReaderRC2 LIMITED $i", time.duration, comparing = avg)

        }
        println()

    }

    fun doubleBufferedReaderRc2ReadToChar(){

        for( i in 1 ..10) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.readTo('\n', matchPosition = HistoryBufferedReader.MatchPosition.AFTER) != null){}
                }
            }
            printInfo("READ TO char DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)

        }
        println()

    }

//    fun doubleBufferedReaderRc2GoForwardToChar1(){
//
//        for( i in 1 ..10) {
//            var foundCnt = 0
//            var cnt = 0
//            var text: String? = null
//
//            val time = measureTimedValue {
//                for (file in files) {
//                    val reader =  DoubleBufferedReaderRC2(file.reader(), 2.0.pow(20).toInt())
//                    while (reader.goForwardTo1('\n', matchPosition = IHistoryBufferedReader.MatchPosition.AFTER)){}
//                }
//            }
//            printInfo("GO FORWARD TO char (1) DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)
//
//        }
//        println()
//
//    }


    fun doubleBufferedReaderRc2GoForwardToStr1(){

        for( i in 1 ..10) {
            var foundCnt = 0
            var cnt = 0
            var text: String? = null

            val time = measureTimedValue {
                for (file in files) {
                    val reader =  DoubleBufferedReader(file.reader())
                    while (reader.goForwardTo1("\n", matchPosition = HistoryBufferedReader.MatchPosition.AFTER)){}
                }
            }
            printInfo("GO FORWARD TO STR (1) DoubleBufferedReaderRC2 $i", time.duration, comparing = avg)

        }
        println()

    }





    @Test
    fun doubleBufferedReaderRc2GoForwardToCharTest(){

        bufferedReader()
        doubleBufferedReaderRc2GoForwardToChar()
//        doubleBufferedReaderRc2GoForwardToChar1()

    }
}