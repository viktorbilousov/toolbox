package org.vib.toolbox.extractor

fun interface IFieldExtractor<IN> {
    fun extract(obj: IN): Map<String, Any?>
}