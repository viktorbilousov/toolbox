package org.vib.toolbox.listrender

import kotlin.collections.iterator
import kotlin.math.max

open class GroupedListRender<IN>(val render: IListRender<IN>): IListRender<Pair<String, List<IN>>>, HasStartNumber {

    private var startNumber = 1;

    protected fun createGroupSeparator(group: String): String{
        return "\n===================================== $group =========================================\n"
    }

    override fun join(entries: List<Pair<String, List<IN>>>): String {
        return join(entries.associate { it })
    }

    fun join(entries: Map<String, List<IN>>): String {
        val sb = StringBuilder()
        var number = startNumber;
        for (entry in entries) {
            sb.append(createGroupSeparator(entry.key))
            if(render is HasStartNumber){
                render.setStartNumber(number);
            }
            sb.append(render.join(entry.value))
            number += entry.value.size
        }
        return sb.toString()
    }

    override fun setStartNumber(startNumber: Int) {
        this.startNumber = max(startNumber, 1);
    }

}