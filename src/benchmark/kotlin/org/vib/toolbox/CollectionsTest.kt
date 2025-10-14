package org.vib.toolbox

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.vib.toolbox.collections.LinkedArray
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class CollectionsTest {

    companion object{

        @BeforeAll
        @JvmStatic
        fun createFiles(){
            Utils.createFiles()
        }
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun LinkedArray(){
        var commonTime = Duration.ZERO;
        val array = LinkedArray<Int>(1000)
        for (file in Utils.files) {
            val ints = file.readBytes().map { it-> it.toInt() }
            val time = measureTime {
                for (i in ints) {
                    array.add(i)
                }
            }
            commonTime += time
        }

        Utils.printInfo("LinkedArray", commonTime)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun ArrayList(){
        var commonTime = Duration.ZERO;
        val array = ArrayList<Int>(1000);

        for (file in Utils.files) {
            val ints = file.readBytes().map { it-> it.toInt() }
            val time = measureTime {
                var index = 0
                var topReach = false;
                for (i in ints) {
                    if(topReach) {
                        array[index] = i
                    }
                    else{
                        array.add(i)
                    }
                    if(index == 999){
                        index = 0
                        topReach = true
                    }
                    else{
                        index++
                    }
                }
            }
            commonTime += time
        }

        Utils.printInfo("LinkedArray", commonTime)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun Array(){
        val size = 1000
        var commonTime = Duration.ZERO;
        val array = Array<Int>(size) {0}

        for (file in Utils.files) {
            val ints = file.readBytes().map { it-> it.toInt() }
            val time = measureTime {
                var index = 0
                for (i in ints) {
                    array[index] = i

                    if(index == size -1){
                        index = 0
                    }
                    else{
                        index++
                    }
                }
            }
            commonTime += time
        }

        Utils.printInfo("LinkedArray", commonTime)
    }


    @Test
    fun Array2(){
        val size = 1000
        var commonTime = Duration.ZERO;
        val array = Array<Int>(size) {0}.toIntArray()

        for (file in Utils.files) {
            val ints = file.readBytes().map { it-> it.toInt() }
            val time = measureTime {
                var index = 0
                val fileLen = ints.size
                while (true){
                    if(fileLen - index > size ){
                    }
                }
            }
            commonTime += time
        }

        Utils.printInfo("LinkedArray", commonTime)
    }



}