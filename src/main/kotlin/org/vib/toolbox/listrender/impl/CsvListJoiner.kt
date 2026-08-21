package org.vib.toolbox.listrender.impl

import org.vib.toolbox.listrender.AListRender

open class CsvListJoiner<IN>() : AListRender<IN>(){

    var delimiter : String = ";"
    var nullvalues: String = ""

    protected open fun printLine(line: List<Any?>, builder: StringBuilder){
        for ((index, obj) in line.withIndex()) {
            if(index != 0){
                builder.append(delimiter)
            }
            builder.append(obj ?: nullvalues)
        }
        builder.append("\n")
    }

    override fun joinEntries(entries: List<Map<String, Any?>>): String {
        if(entries.isEmpty()) return ""
        val sb = StringBuilder()
        if(printLegend){
            printLine(entries.first().keys.toList(), sb)
        }

        for (map in entries) {
            printLine(map.values.toList(), sb)
        }

        return sb.toString()
    }

}