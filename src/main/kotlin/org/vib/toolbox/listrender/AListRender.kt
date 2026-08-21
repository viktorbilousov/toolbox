package org.vib.toolbox.listrender

import org.vib.toolbox.extractor.GroupFieldExtractor
import org.vib.toolbox.extractor.IFieldExtractor

abstract class AListRender<IN>() : IListRender<IN>, HasStartNumber {

    constructor(
        defaultColumns: List<String>,
        valueExtractors: List<IFieldExtractor<IN>>,
        printNumber: Boolean = true,
        beginNumber: Int = 1,
        printLegend: Boolean = false,
        numberFormatter: NumberFormatter = DEFAULT_NUMBER_EXTRACTOR
    ) : this() {
        this.columnsNames.addAll(defaultColumns)
        this.valueExtractors.addAll(valueExtractors)
        this.printLegend = printLegend
        this.beginNumber = beginNumber
        this.printNumber = printNumber
        this.numberFormatter = numberFormatter
    }

    private val extractor : GroupFieldExtractor<IN> = GroupFieldExtractor()

    var printNumber: Boolean = true
    var beginNumber: Int = 1
    var printLegend: Boolean = false
    var numberFormatter: NumberFormatter = NumberFormatter { it.toString() }
    var currentCounter = beginNumber;


    private fun createNumberExtractor() : IFieldExtractor<IN>{
        return object : SingleFieldExtractor<IN>(RECORD_NUMBER){
            override fun extractField(transaction: IN): String? {
                return numberFormatter.invoke(currentCounter++)
            }
        }
    }

    override fun setStartNumber(startNumber: Int) {
        this.beginNumber = startNumber;
    }

    companion object {
        val UNDEFINED = ">UNDEFINED<"
        val RECORD_NUMBER = "#"
        private val DEFAULT_NUMBER_EXTRACTOR = NumberFormatter { it.toString() }
    }

    val columnsNames: MutableList<String> = extractor.columnsNames
    val valueExtractors: MutableList<IFieldExtractor<IN>> = extractor.valueExtractors

    init {
        if (printNumber && !columnsNames.contains(RECORD_NUMBER)) {
            this.columnsNames.add(0, RECORD_NUMBER)
        }


    }

    fun resetRecordNumber() {
        currentCounter = beginNumber
    }

    protected abstract fun joinEntries(entries: List<Map<String, Any?>>): String

    override fun join(entries: List<IN>): String {

        val numberExtractor = createNumberExtractor()

        if (printNumber) {
            valueExtractors.add(numberExtractor)
        }

        try {
            val entriesList = mutableListOf<Map<String, Any?>>()
            for (entry in entries) {
                val map = extractor.extract(entry)
                entriesList.add(map)
                if (printNumber) {
                    if (!map.contains(RECORD_NUMBER)) {
                        map
                    }
                }
            }
            val res = joinEntries(entriesList)
            return res;
        } finally {
            valueExtractors.remove(numberExtractor)
        }
    }
}