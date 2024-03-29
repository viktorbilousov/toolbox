package com.systema.kotlin.toolbox.parser

import com.systema.kotlin.toolbox.StringStreamReader

interface IParser<EL> {

    fun parse(string: String, strict: Boolean = false) : EL{
        return parse(StringStreamReader(string), strict)
    }

    fun parse(reader: StringStreamReader, strict: Boolean = false) : EL
}
