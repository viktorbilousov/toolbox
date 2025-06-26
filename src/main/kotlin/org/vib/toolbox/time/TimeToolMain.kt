package org.vib.toolbox.time

import org.vib.toolbox.logger
import kotlin.math.abs
import kotlin.random.Random

private object TimeToolMain {

    val logger = logger<TimeTool>()

    val sleepC = mutableListOf<Long>()

    @JvmStatic
    fun main(args: Array<String>) {
        TimeTool.enabled = true;
        TimeTool.enableAll()
        TimeTool.beginMeasure(this)
        functionA()
        TimeTool.endMeasure(this)
        TimeTool.printTotal(){ println(it)}

        println("Avarege time "  + sleepC.average())
        println("Common time "  + sleepC.sum())
    }

    fun functionA(){
        println("Call A")
        TimeTool.beginMeasure(this, "functionA")
        functionB()
        functionB()
        functionB()
        TimeTool.endMeasure(this, "functionA")
        println("End A")
    }

    fun functionB(){
        println("Call B")
        TimeTool.beginMeasure(this, "functionB")
        functionC()
        functionC()
        functionC()
        TimeTool.endMeasure(this, "functionB")
        println("End B")

    }

    fun functionC(){
        println("Call C")
        TimeTool.beginMeasure(this, "functionC")
        val sleep = abs(Random.nextLong()) % 1000 + 1;
        sleepC.add(sleep)
        println("Sleep $sleep")
        Thread.sleep(sleep)
        TimeTool.endMeasure(this, "functionC")
        println("End C")
    }


}