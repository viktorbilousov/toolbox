@file:Suppress("unused")

package org.vib.toolbox

import org.vib.toolbox.time.HasTime
import org.vib.toolbox.time.between
import java.time.Instant

object NumberedMapsUtils {

    private val logger = logger(this)

    fun <T> wrapToNumberedList(list: Collection<T>, beginNumber: Int = 1): NumberedMap<T> {
        return wrapToSortedNumberedList(list, beginNumber){ o1, o2 -> 1  }
    }

    fun <T> wrapToSortedNumberedList(list: Collection<T>, beginNumber: Int = 1, sorter: Comparator<T> = Comparator { o1, o2 -> 1  }): NumberedMap<T> {
        return list.sortedWith(sorter)
            .mapIndexed { index, t -> (beginNumber + index) to t }
            .associate { it }
            .toMutableMap()
    }

    fun <T : HasTime> wrapToNumberedListSortedByTime(list: Collection<T>, beginNumber: Int = 1): NumberedMap<T> {
        require(list.none { it.time == null }) {"Cannot sort transaction by time: time is null!"}
        return wrapToSortedNumberedList(list, beginNumber) { o1, o2 -> o1.time!!.compareTo(o2.time!!) }
    }

    fun <T : HasTime> wrapToNumberedListSortedByTimeDesc(list: Collection<T>, beginNumber: Int = 1): NumberedMap<T> {
        require(list.none { it.time == null }) {"Cannot sort transaction by time: time is null!"}
        return wrapToSortedNumberedList(list, beginNumber) { o1, o2 -> o2.time!!.compareTo(o1.time!!) }
    }


    fun <T: HasTime> filter(entry: NumberedMap<T>, from: Instant, to: Instant, includeTimeNull: Boolean = false, ignoreTimeNull: Boolean = false): NumberedMap<T> {
        return entry.filterValues { it.between(from, to, includeTimeNull, ignoreTimeNull) }.toMutableMap()
    }


    fun<T: HasTime> filter(entries: List<T>,
                           from: Instant,
                           to: Instant,
                           includeTimeNull: Boolean = false,
                           allowNulls: Boolean = false): List<T> {
        return entries.filter {
            it.between(from, to, includeTimeNull, allowNulls)
        }
    }

    fun<T> removeNumbersGaps(numberedMap: NumberedMap<T>) : NumberedMap<T>{
        val first = numberedMap.keys.first()
        return wrapToNumberedList(numberedMap.values, first)
    }

    fun <T> updateNumbers(numberedMap: NumberedMap<T>, beginNumber: Int) : NumberedMap<T>{
        return wrapToNumberedList(numberedMap.values, beginNumber)
    }


    fun <K, T: HasTime> sortMapByTime(map: Map<K, Collection<T>>, sortKeys: Boolean = true) : Map<K, NumberedMap<T>> {
        var sortedValuesMap = map.mapValues { it.value.sortedBy { it.time } }
        if (sortKeys) {
            sortedValuesMap = sortedValuesMap.entries.sortedBy { it.value.first().time }.associate { it.toPair() }
        }

        var number = 1
        return sortedValuesMap.mapValues { wrapToNumberedList(it.value, number).apply { number += this.size } }
    }



}