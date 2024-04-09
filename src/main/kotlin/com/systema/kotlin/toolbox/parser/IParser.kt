package com.systema.kotlin.toolbox.parser

import com.systema.kotlin.toolbox.StringStreamReader

interface IParser<EL> {

    fun parse(string: String) : EL = parse(StringStreamReader(string))

    fun parse(reader: StringStreamReader) : EL
}
