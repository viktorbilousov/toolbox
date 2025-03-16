package org.vib.toolbox.time

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

class Stopwatch {
    private val measurements = mutableMapOf<String, MutableList<Long>>()
    private val startedMeasurements = mutableMapOf<String, Instant>()

    companion object{
        private val instances = mutableMapOf<String, Stopwatch>()
        fun instanceFor(name: String) = instances.getOrPut(name) { Stopwatch() }
        val instance = instanceFor("Default")
    }

    fun<T> measure(type: String = "default", action: () -> T ) : T{
        startMeasure(type)
        val res = action()
        stopMeasure(type)
        return res
    }



    fun startMeasure(type: String = "default"){
        startedMeasurements[type] = Instant.now()
    }

    fun stopMeasure(type: String = "default"){
        val current = Instant.now()
        val prev =startedMeasurements[type] ?: return
        measurements.getOrPut(type){ mutableListOf()}.add(current.toEpochMilli() - prev.toEpochMilli())
        startedMeasurements.remove(type)
    }

    fun reset() {
        measurements.clear()
    }

    fun print(group: Boolean = true){
        for (measurement in measurements.entries.sortedBy { it.key }) {
            if(group){
                val sum = measurement.value.sum()
                println(measurement.key + "\t\t " + sum.toDouble()/1000 + " calls: ${measurement.value.size}, avg: ${measurement.value.average()}")
            }
            else {
                println(measurement.key)
                measurement.value.forEachIndexed { index, l ->
                    println("\t\t ${index+1} : $l")
                }
            }
        }
    }

    fun total() : Duration {
       return measurements.entries.sumOf { it.value.sum() }.milliseconds
    }

    fun measurements() = measurements.toMap()

}