@file:Suppress("unused", "MemberVisibilityCanBePrivate", "NestedLambdaShadowedImplicitParameter")

package com.systema.kotlin.toolbox

/**
 * version 1.0
 * */
object TablePrinter {

    private const val NONE_VALUE = "-"

    fun printTableWithLegend(
        rows: Collection<Map<String, Any?>>,
        marginSpaces: Int = 5,
        noneValue: String = NONE_VALUE,
        printHeader: Boolean = true,
    ): String {
        return printTableWithLegend(rows, marginSpaces, noneValue, printHeader){it}
    }


    fun printTable(
        rows: Collection<Map<String, Any?>>,
        marginSpaces: Int = 5,
        noneValue: String = NONE_VALUE,
        ): String {
        return printTableWithLegend(rows, marginSpaces, noneValue, false){it}
    }
    fun <T> printTableWithLegend(
        rows: Collection<T>,
        marginSpaces: Int = 5,
        noneValue: String = NONE_VALUE,
        printHeader: Boolean = true,
        extractColumns: (T) -> Map<String, Any?> = { mapOf("value" to it) },
    ): String {
        if(rows.isEmpty()) return ""
        val colsAndLegend = rows.map { extractColumns(it) }.toMutableList()
        val legend2Index = mutableMapOf<String, Int>()
        colsAndLegend.forEach {
            if(legend2Index.isEmpty()){
                it.entries.forEachIndexed { i, s ->
                    legend2Index[s.key] = i
                }
            }
            else if(!legend2Index.keys.containsAll(it.keys)) {
                val keysToAdd : List<Pair<String, Int>> = it.keys
                    .mapIndexed { index, s -> s to index }
                    .filterNot {  legend2Index.containsKey(it.first) }

                val map = it
                keysToAdd.forEach { pair ->
                    if(!legend2Index.containsValue(pair.second)){
                        legend2Index[pair.first] = pair.second
                    }
                    else {
                        var prevLegend: String ? = null
                        var nextLegend: String ? = null
                        val newLegends = map.keys.toList()
                        val index = newLegends.indexOf(pair.first)
                        if(index > 0){
                            prevLegend = newLegends[index-1]
                            if(!legend2Index.containsKey(prevLegend)){
                                prevLegend = null
                            }
                        }
                        if(index != newLegends.lastIndex){
                            nextLegend = newLegends[index+1]
                            if(!legend2Index.containsKey(nextLegend)){
                                nextLegend = null
                            }
                        }

//                        if(nextLegend == null && prevLegend == null){
//                            error("cannot merge column ${it.first}")
//                        }
                        val prevIndex  =
                            if(prevLegend != null){
                                legend2Index[prevLegend]!!

                            }
                            else if(nextLegend != null) {
                                legend2Index[nextLegend]!! - 1
                            }
                             else {  -1  }

                        legend2Index.forEach {
                            if(it.value > prevIndex) {
                                legend2Index[it.key] = it.value + 1
                            }
                        }
                        legend2Index[pair.first] = prevIndex + 1
                    }

                }

            }
        }
        val legend = legend2Index.entries.sortedBy { it.value }.map { it.key }
        val extractedRows = colsAndLegend.map {
            if(it.keys.containsAll(legend)) {
                it.values.toList()
            }
            else{
                val map = mutableMapOf<String, Any?>()
                for (s in legend) {
                    map[s] = it[s]
                }
                map.values.toList()
            }
        }.toMutableList()
        if(printHeader) {
            extractedRows.add(0, legend)
            extractedRows.add(1, legend.map { " " })
        }
        return printTable(extractedRows, marginSpaces, noneValue) { it }
    }

    fun <T> printTable(
        rows: List<T>,
        marginSpaces: Int = 5,
        noneValue: String = NONE_VALUE,
        extractColumns: (T) -> List<Any?> = { listOf(it as Any?) },
    ): String {
        if(rows.isEmpty()) return ""
        val rowsCols = rows.map { extractColumns(it).map { it?.toString() } }
        val columnsCnt = rowsCols.map { it.size }.maxBy { it }
        val maxColLen = mutableMapOf<Int, Int>()
        for(i in 0..columnsCnt) {
            maxColLen[i] = rowsCols.map { it.getOrNull(i)?.length ?: 0 }.maxBy { it }
        }
        val sb = StringBuilder()

        rowsCols.forEach {
            val row = it
            for(i in 0 until columnsCnt) {
                val str = row.getOrNull(i) ?: printInCenter(noneValue, maxColLen[i]!!)
                if(i < columnsCnt - 1) {
                    val spaces = spaces(maxColLen[i]!! - str.length + marginSpaces)
                    sb.append(str).append(spaces)
                } else {
                    sb.append(str)
                }
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    private fun printInCenter(string: String, colLen: Int): String {
        val len = colLen - string.length
        if(len == 0) return string
        val sb = StringBuilder()
        sb.append(spaces((len.toDouble()/2).toInt()))
        sb.append(string)
        sb.append(spaces(colLen - sb.length))
        return sb.toString()
    }


    private fun spaces(cnt: Int): String {
        val sb = StringBuilder()
        for (i in 1..cnt) {
            sb.append(" ")
        }
        return sb.toString()
    }

}

