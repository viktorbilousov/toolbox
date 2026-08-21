package org.vib.toolbox.listrender.impl

import org.vib.toolbox.listrender.AListRender
import org.vib.toolbox.listrender.HasStartNumber
import org.vib.toolbox.table.TablePrinter

open class ListToTableRender<IN>() : AListRender<IN>() {

    protected fun join(entries: List<Map<String, Any?>>, printLegend: Boolean): String {
        return TablePrinter.printTableWithLegend(entries, 10, printLegend, false) { it }
    }

    override fun joinEntries(entries: List<Map<String, Any?>>): String {
        return join(entries, this.printLegend)
    }
}