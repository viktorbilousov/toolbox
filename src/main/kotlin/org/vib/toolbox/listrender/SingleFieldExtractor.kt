package org.vib.toolbox.listrender

import org.vib.toolbox.extractor.IFieldExtractor

abstract class SingleFieldExtractor<IN>(val fieldName: String): IFieldExtractor<IN> {

    final override fun extract(obj: IN): Map<String, Any?> {
        val field = extractField(obj);
        return mapOf(fieldName to field)
    }

    abstract fun extractField(transaction: IN): String?
}