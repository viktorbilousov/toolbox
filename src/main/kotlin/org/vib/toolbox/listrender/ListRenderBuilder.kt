package org.vib.toolbox.listrender

import org.vib.toolbox.builders.MapBuilder
import org.vib.toolbox.extractor.IFieldExtractor

open class ListRenderBuilder<IN, Render: AListRender<IN>>(printer: Render) {

    val printer: Render = printer

    fun column(vararg column: String) = apply {
        printer.columnsNames.addAll(column)
    }

    open fun columns(column: Collection<String>) = apply {
        printer.columnsNames.addAll(column)
    }

    fun columnsSorted(columns: Map<Int, String>){
        columns(columns.entries.sortedBy { it.key }.map { it.value })
    }

    fun columnsSorted(columns: MapBuilder<Int, String>.() -> Unit){
        val map = MapBuilder<Int, String>().apply(columns).build()
        columns(map.entries.sortedBy { it.key }.map { it.value })
    }

    fun extractor(extractor: IFieldExtractor<IN>) = apply {
        printer.valueExtractors.add(extractor)
    }

    fun extractors(extractor: Collection<IFieldExtractor<IN>>) = apply {
        printer.valueExtractors.addAll(extractor)
    }



    fun extractor(extract: (IN) -> Map<String, Any?>) = apply {
        val extractor = object : IFieldExtractor<IN> {
            override fun extract(obj: IN): Map<String, Any?> = extract(obj)
        }
        extractor(extractor)
    }

    fun numberFormatter(numberFormatter: NumberFormatter) = apply{
        printer.numberFormatter = numberFormatter
    }

    fun numberFormatter(format: (Int) -> String) = apply {
        printer.numberFormatter = object : NumberFormatter{
            override fun invoke(number: Int): String  = format(number)
        }
    }

    var printNumbers : Boolean
        get() = printer.printNumber
        set(value) {printer.printNumber = value}

    var beginNumber : Int
        get() = printer.beginNumber
        set(value) {printer.beginNumber = value}


    var printLegend : Boolean
        get() = printer.printLegend
        set(value) {printer.printLegend = value}

   open fun build(): Render{
        return printer;
    }

    companion object{
        fun <IN> configure(printer: AListRender<IN>, builder: ListRenderBuilder<IN, AListRender<IN>>.() -> Unit): AListRender<IN> {
            return ListRenderBuilder<IN, AListRender<IN>>(printer).apply(builder).build()
        }

        fun <IN> create(printer: () -> AListRender<IN>, builder: ListRenderBuilder<IN, AListRender<IN>>.() -> Unit): AListRender<IN> {
            return configure(printer(), builder)
        }

        fun <IN, Render: AListRender<IN>> configureT(printer: Render, builder: ListRenderBuilder<IN, Render>.() -> Unit): Render {
            return ListRenderBuilder<IN, Render>(printer).apply(builder).build()
        }

        fun <IN, Render: AListRender<IN>> createT(printer: () -> Render, builder: ListRenderBuilder<IN, Render>.() -> Unit): Render {
            return configureT(printer(), builder)
        }
    }

}