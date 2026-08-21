package org.vib.toolbox.extractor

import org.vib.toolbox.ignoreException

open class GroupFieldExtractor<IN>() : IFieldExtractor<IN>{

    companion object {
        val UNDEFINED = ">UNDEFINED<"
    }

    val columnsNames: MutableList<String> = mutableListOf()
    val valueExtractors: MutableList<IFieldExtractor<IN>> = mutableListOf()


    protected fun extractFields(obj: IN): Map<String, Any?> {
        val col2Values = mutableMapOf<String, Any?>()
        for (extractor in valueExtractors) {
            ignoreException {
                col2Values.putAll(extractor.extract(obj).filterValues { it != null })
            }
        }

        return col2Values
    }


    protected fun extractFields(obj: IN, number: Int): Map<String, Any?> {
        val col2Values = extractFields(obj) as MutableMap<String, Any?>
        return col2Values
    }


    protected open fun sortKeys(list: List<String>): List<String> {
        val unknownNames = list.filter { it !in columnsNames }.sorted()
        val sortedList = mutableListOf<String>()
        for (col in columnsNames) {
            if (col == UNDEFINED) {
                sortedList.addAll(unknownNames)
                continue
            }
            sortedList.add(col)
        }

        return sortedList;
    }


    protected fun orderMap(
        map: Map<String, *>,
        keys: List<String>,
        applyMissingFields: Boolean = true
    ): Map<String, *> {
        val sortedMap = linkedMapOf<String, Any?>()
        for (key in keys) {
            if (map[key] == null && !applyMissingFields) continue
            sortedMap[key] = map[key]
        }
        return sortedMap;
    }

    protected open fun getLegend(columns: List<String>): Map<String, String> {
        return columns.associateWith { it }
    }

    protected open fun entriesToKeyValueMap(
        entry: IN,
        applyMissingFields: Boolean = true
    ): Map<String, Any?> {
        val columns = columnsNames.toMutableSet()
        val fields = extractFields(entry).toMutableMap()
        columns.addAll(fields.keys)
        val sortedKeys = sortKeys(columns.toList())
        return orderMap(fields, sortedKeys, applyMissingFields)
    }


    override fun extract(obj: IN): Map<String, Any?> {

        require(this.columnsNames.isNotEmpty()) { "Columns are empty" }
        require(this.valueExtractors.isNotEmpty()) { "Columns are empty" }


        return entriesToKeyValueMap(obj, true);
    }

}