package com.systema.kotlin.toolbox.table

data class TableMarginInfo(val maxColLen: Map<Int, Int>, val marginSpaces: Int) {
    override fun toString(): String {
        val cols = maxColLen.entries.sortedBy { it.key }.map { it.value }
        val sb = StringBuilder("@TI:")
        sb.append("M_$marginSpaces")
        cols.forEachIndexed { index, i ->
            sb.append(" C${index}_$i")
        }
        return sb.toString()
    }

    companion object {

        fun isLineMarginInfo(line: String) = line.startsWith("@TI:")

        private fun parseError(line: String, string: String): Nothing {
            error("Cannot parse TableMarginInfo from line $line: $string")
        }

        fun parse(line: String): TableMarginInfo {
            val tokens = line.substringAfter("@TI:").trim().split(" ").map { it.trim() }.filter { it.isNotEmpty() }
            require(tokens.isNotEmpty()) { "Cannot parse TableMarginInfo from line $line: empty tokens!" }

            val margin = tokens.firstOrNull { it.startsWith("M_") }?.let {
                it.replace("M_", "").toIntOrNull() ?: parseError(line, "illegal margin token $it")
            } ?: parseError(line, "margin token is null")

            val columns = tokens.filter { it.startsWith("C") }.associate {
                val colIndex =
                    it.substringAfter("C").substringBefore("_").toIntOrNull() ?: parseError(
                        line,
                        "illegal colum token $it"
                    )
                val colMargin = it.substringAfter("_").toIntOrNull() ?: parseError(line, "illegal colum token $it")

                colIndex to colMargin
            }

            return TableMarginInfo(columns, margin)
        }
    }
}
