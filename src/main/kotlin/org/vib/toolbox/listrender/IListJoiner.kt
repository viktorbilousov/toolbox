package org.vib.toolbox.listrender

interface IListJoiner<IN, OUT>{
    fun join(entries: List<IN> ): OUT
}