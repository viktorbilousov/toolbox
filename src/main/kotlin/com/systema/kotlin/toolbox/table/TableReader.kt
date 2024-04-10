package com.systema.kotlin.toolbox.table

object TableReader {

    fun parse(table: String, skipHeader: Boolean = false) : Map<Int, List<String>> {
        val lines = table.lines()
        val index = lines.indexOfFirst { TableMarginInfo.isLineMarginInfo(it) }
        require(index != -1) {"Cannot find Table Margin Info in a table!"}
        val marginHeader: TableMarginInfo = parseTableInfo(lines[index])

        return parse(lines.subList(index, lines.size), marginHeader, skipHeader)
    }

    fun parse(table: String, marginInfo: TableMarginInfo, skipHeader: Boolean = false,) : Map<Int, List<String>> {
        val lines = table.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return parse(lines, marginInfo, skipHeader)
    }

    fun parse(lines: Collection<String>, marginInfo: TableMarginInfo, skipHeader: Boolean = false) : Map<Int, List<String>> {
        val rows = mutableMapOf<Int, List<String>>()
        val notEmptyLines = lines.map { it.trim() }.filter { it.isNotEmpty() }
        var first = skipHeader
        var index = 0
        for (line in notEmptyLines) {
            if(TableMarginInfo.isLineMarginInfo(line)) continue
            if(first) {
                first = false
                continue
            }
            val row = parseLine(line, marginInfo)
            rows[index++] = row
        }
        return rows
    }


    //@TI:M_5 C0_5 C1_23 C2_17 C3_0 C4_18972 C5_0
    private fun parseLine(line: String, marginInfo: TableMarginInfo): List<String> {
        val parts = mutableListOf<String>()
        var shift = 0
        val lastIndex = marginInfo.maxColLen.entries.last().key
        for ((index, collen) in marginInfo.maxColLen.entries) {
            val part = if (index < lastIndex)
            {
                line.substring(shift, collen).trim()
            }
            else if(collen > 0)
            {
                line.substring(shift, line.length).trim()
            }
            else
            {
                ""
            }

            shift += collen + marginInfo.marginSpaces
            parts.add(part)
        }
        return parts
    }


    fun parseTableInfo(tableInfoLine: String) : TableMarginInfo {
        return TableMarginInfo.parse(tableInfoLine)
    }

}